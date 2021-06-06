package com.dreamlike.ocean.ByteMsg.Allocator;

import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;

public interface ByteMsgAllocator {


    void release(ByteMsg byteMsg);

    ByteMsg allocate(int size);


}
