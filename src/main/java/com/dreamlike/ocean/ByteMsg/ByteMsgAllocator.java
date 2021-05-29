package com.dreamlike.ocean.ByteMsg;

import com.dreamlike.ocean.ByteMsg.AbstractByteMsg;
import com.dreamlike.ocean.ByteMsg.ByteMsg;

public interface ByteMsgAllocator {


    void release(ByteMsg byteMsg);

    ByteMsg allocate(int size);


}
