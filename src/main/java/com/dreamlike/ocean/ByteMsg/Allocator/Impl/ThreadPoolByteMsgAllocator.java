package com.dreamlike.ocean.ByteMsg.Allocator.Impl;

import com.dreamlike.ocean.ByteMsg.Allocator.PoolByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.ByteMsg.Msg.impl.FixedPoolByteMsg;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

//线程私有
public class ThreadPoolByteMsgAllocator implements PoolByteMsgAllocator {
    public static final ThreadPoolByteMsgAllocator DEFAULT_INSTANT = new ThreadPoolByteMsgAllocator(SharedByteMsgAllocator.DEFAULT_INSTANT,4*1024,128);
    private ByteBuffer rawSpan;
    private SharedByteMsgAllocator parent;
    private int poolSize;
    private boolean isInit;
    private int chunkSize;
    private Queue<FixedPoolByteMsg> raw;

    public ThreadPoolByteMsgAllocator(SharedByteMsgAllocator sharedByteMsgAllocator,int poolSize,int chunkSize){
        parent = sharedByteMsgAllocator;
        int i = poolSize / (4*1024);
        //4K 对齐
        this.poolSize = (i <= 0 ? 1 : i) * (4*1024);
        isInit = false;
        if (poolSize % chunkSize != 0){
            throw new IllegalArgumentException("块大小必须为4k的因子");
        }
        this.chunkSize = chunkSize;
        raw = new LinkedList<>();
    }

    @Override
    public void release(ByteMsg byteMsg) {
        if (byteMsg instanceof FixedPoolByteMsg && byteMsg.allocator() == this){
            raw.offer((FixedPoolByteMsg) byteMsg);
        }
    }

    @Override
    public FixedPoolByteMsg allocate(int size) {
        if (!isInit){
            init();
        }
        FixedPoolByteMsg poll = raw.poll();
        if (poll == null){
            poll = parent.allocate(size);
        }
        return poll;
    }

    private void init(){
        //4 2 2
        int posCount = 0;
        rawSpan = ByteBuffer.allocateDirect(poolSize);
        while ((posCount+1) * chunkSize <= poolSize){
            ByteBuffer node = rawSpan
                    .position(posCount * chunkSize)
                    .limit(posCount * chunkSize + chunkSize)
                    .slice();
            raw.offer(new FixedPoolByteMsg(node, this));
            posCount++;
        }
    }

    @Override
    public int chunkSize() {
        return chunkSize;
    }
}
