package com.dreamlike.ocean;

import com.dreamlike.ocean.ByteMsg.Allocator.Impl.ThreadPoolByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.impl.FlexPoolByteMsg;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException{
        ThreadPoolByteMsgAllocator allocator = new ThreadPoolByteMsgAllocator(null, 4000, 8);
        FlexPoolByteMsg msg = new FlexPoolByteMsg(allocator);


    }



}
