package com.dreamlike.ocean.Handler;

import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;

import java.io.IOException;

public interface ChannelReadHandler extends ChannelHandler{
    default void read(MessageHandlerContext mhc, Object msg) throws Throwable {
        mhc.nextRead(msg);
    }
}
