package com.dreamlike.ocean.ByteMsg.Msg.impl;

import com.dreamlike.ocean.ByteMsg.Allocator.ByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Allocator.Impl.ThreadPoolByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.ExtendableByteMsg;
import com.dreamlike.ocean.Channel.Channel;

import java.io.IOException;



public class FlexPoolByteMsg extends CombinedFixedByteMsg implements ExtendableByteMsg {

    private ThreadPoolByteMsgAllocator allocator;

    public FlexPoolByteMsg(ThreadPoolByteMsgAllocator nowAllocator) {
        allocator = nowAllocator;
    }

    @Override
    public FlexPoolByteMsg writeBytes(byte[] bytes) {
        int needToWrite = bytes.length;
        while (writeSize + needToWrite > maxCapacity){
            resize();
        }
        return (FlexPoolByteMsg) writeBytes0(bytes, needToWrite);
    }

    @Override
    public FlexPoolByteMsg writByte(byte b) {
        return (FlexPoolByteMsg) super.writByte(b);
    }

    @Override
    public FlexPoolByteMsg writeChar(char c) {
        return (FlexPoolByteMsg) super.writeChar(c);
    }

    @Override
    public FlexPoolByteMsg writeShort(short s) {
        return (FlexPoolByteMsg) super.writeShort(s);
    }

    @Override
    public FlexPoolByteMsg writeInt(int a) {
        return (FlexPoolByteMsg) super.writeInt(a);
    }

    @Override
    public FlexPoolByteMsg writeLong(long l) {
        return (FlexPoolByteMsg) super.writeLong(l);
    }

    @Override
    public FlexPoolByteMsg writeDouble(double d) {
        return (FlexPoolByteMsg) super.writeDouble(d);
    }

    @Override
    public FlexPoolByteMsg writeFloat(float f) {
        return (FlexPoolByteMsg) super.writeFloat(f);
    }


    @Override
    public ByteMsgAllocator allocator() {
        return allocator;
    }

    @Override
    //有OOM风险 不提供这个支持
    public int readFromChannel(Channel channel) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resize() {
        FixedPoolByteMsg msg = allocator.allocate(allocator.chunkSize());
        addFixedPoolByteMsg(msg);
    }
}
