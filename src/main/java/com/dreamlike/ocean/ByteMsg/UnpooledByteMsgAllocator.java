package com.dreamlike.ocean.ByteMsg;

import java.nio.ByteBuffer;

public class UnpooledByteMsgAllocator implements ByteMsgAllocator{
    private static final UnpooledByteMsgAllocator unpooledByteMsgAllocator = new UnpooledByteMsgAllocator();
    @Override
    public void release(ByteMsg byteMsg) {

    }

    @Override
    public ByteMsg allocate(int size) {
        return new UnpooledByteMsg(ByteBuffer.allocateDirect(size));
    }
    public static UnpooledByteMsgAllocator get(){
        return unpooledByteMsgAllocator;
    }

}
