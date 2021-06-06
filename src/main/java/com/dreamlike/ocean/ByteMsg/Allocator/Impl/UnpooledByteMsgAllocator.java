package com.dreamlike.ocean.ByteMsg.Allocator.Impl;

import com.dreamlike.ocean.ByteMsg.Allocator.ByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.ByteMsg.Msg.impl.UnpoolByteMsg;

import java.nio.ByteBuffer;

public class UnpooledByteMsgAllocator implements ByteMsgAllocator {
    private static final UnpooledByteMsgAllocator unpooledByteMsgAllocator = new UnpooledByteMsgAllocator();
    @Override
    public void release(ByteMsg byteMsg) {
        //你等GC回收ByteBuffer就行
    }

    @Override
    public UnpoolByteMsg allocate(int size) {
        return new UnpoolByteMsg(ByteBuffer.allocateDirect(size));
    }
    public static UnpooledByteMsgAllocator get(){
        return unpooledByteMsgAllocator;
    }



}
