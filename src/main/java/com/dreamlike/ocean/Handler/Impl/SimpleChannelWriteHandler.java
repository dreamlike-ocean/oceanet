package com.dreamlike.ocean.Handler.Impl;

import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.Handler.ChannelWriteHandler;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;

public abstract class SimpleChannelWriteHandler<I> implements ChannelWriteHandler {
    public abstract void write(MessageHandlerContext messageHandlerContext, I msg, ByteMsg byteMsg);

    @Override
    public void write(MessageHandlerContext mhc, Object msg) throws Throwable {
        ByteMsg byteMsg = mhc.channel().eventLoop().getByteMsgAllocator().allocate(128);
        write(mhc,(I)msg, byteMsg);

    }
}
