package com.dreamlike.ocean.ByteMsg.Allocator.Impl;

import com.dreamlike.ocean.ByteMsg.Allocator.PoolByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.ByteMsg.Msg.impl.FixedPoolByteMsg;
import com.dreamlike.ocean.Util.AllocatorUtil;

import java.nio.ByteBuffer;

//线程私有
public class ThreadPoolByteMsgAllocator implements PoolByteMsgAllocator {
    public static final ThreadPoolByteMsgAllocator DEFAULT_INSTANT = new ThreadPoolByteMsgAllocator(
            SharedByteMsgAllocator.DEFAULT_INSTANT, 4 * 1024, 128);
    private ByteBuffer rawSpan;
    private final SharedByteMsgAllocator parent;
    private final int poolSize;
    private boolean isInit;
    private final int chunkSize;
    private final ByteBuffer rawBuffer;
    private final int[] longest;
    private final int chunkNum;

    public ThreadPoolByteMsgAllocator(SharedByteMsgAllocator sharedByteMsgAllocator, int poolSize, int chunkSize) {
        parent = sharedByteMsgAllocator;
        int i = poolSize / (4 * 1024);
        //4K 对齐
        this.poolSize = (i <= 0 ? 1 : i) * (4 * 1024);
        this.isInit = false;
        if (poolSize % chunkSize != 0) {
            throw new IllegalArgumentException("块大小必须为4k的因子");
        }

        this.rawBuffer = ByteBuffer.allocateDirect(this.poolSize);
        this.chunkSize = chunkSize;
        this.chunkNum = poolSize / chunkSize;
        this.longest = new int[2 * this.chunkNum - 1];
    }

    @Override
    public void release(ByteMsg byteMsg) {
        if (!(byteMsg instanceof FixedPoolByteMsg) || byteMsg.allocator() != this) {
            return;
        }
        var offset = ((FixedPoolByteMsg) byteMsg).getOffset();
        var nodeSize = 1;
        var index = offset + chunkNum - 1;
        for (; longest[index] != 0; index = AllocatorUtil.parent(index)) {
            nodeSize *= 2;
            if (index == 0)
                return;
        }
        longest[index] = nodeSize;

        // merge contiguous free chunks
        while (index != 0) {
            index = AllocatorUtil.parent(index);
            nodeSize *= 2;

            var leftLongest = longest[AllocatorUtil.leftLeaf(index)];
            var rightLongest = longest[AllocatorUtil.rightLeaf(index)];

            if (leftLongest + rightLongest == nodeSize) {
                longest[index] = nodeSize;
            } else {
                longest[index] = Math.max(leftLongest, rightLongest);
            }
        }
    }

    @Override
    public FixedPoolByteMsg allocate(int size) {
        if (!isInit) {
            init();
        }
        var chunks = size / chunkSize;
        // chunks need to be allocated
        chunks = size % chunkSize == 0 ? AllocatorUtil.roundUp(chunks) : AllocatorUtil.roundUp(chunks + 1);
        if (longest[0] < chunks) {
            var poll = parent.allocate(chunks);
            poll.hold();
            return poll;
        } else {
            int nodeSize;
            int offset;
            int index = 0;

            for (nodeSize = chunkNum; nodeSize != chunks; nodeSize /= 2) {
                var left = longest[AllocatorUtil.leftLeaf(index)];
                var right = longest[AllocatorUtil.rightLeaf(index)];
                // 优先选择最小的且满足条件的分叉，小块优先，尽量保留大块
                if (left <= right) {
                    if (left >= chunks)
                        index = AllocatorUtil.leftLeaf(index);
                    else
                        index = AllocatorUtil.rightLeaf(index);
                } else {
                    if (right >= chunks)
                        index = AllocatorUtil.rightLeaf(index);
                    else
                        index = AllocatorUtil.leftLeaf(index);
                }
            }

            longest[index] = 0;
            offset = (index + 1) * nodeSize - chunkNum;

            while (index != 0) {
                index = AllocatorUtil.parent(index);
                longest[index] = Math.max(longest[AllocatorUtil.leftLeaf(index)],
                                          longest[AllocatorUtil.rightLeaf(index)]);
            }

            var byteMsg = rawBuffer.limit(poolSize)
                                   .position(offset * chunkSize)
                                   .limit((offset + chunks) * chunkSize)
                                   .slice();
            var poll = new FixedPoolByteMsg(byteMsg, this, offset);
            poll.hold();
            return poll;
        }
    }

    private void init() {
        var nodeSize = chunkNum * 2;
        for (int i = 0; i < longest.length; i++) {
            if (AllocatorUtil.isPowerOf2(i + 1)) {
                nodeSize /= 2;
            }
            longest[i] = nodeSize;
        }
        isInit = true;
    }

    @Override
    public int chunkSize() {
        return chunkSize;
    }
}
