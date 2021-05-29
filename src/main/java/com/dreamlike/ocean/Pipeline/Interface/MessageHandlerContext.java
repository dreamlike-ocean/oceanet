package com.dreamlike.ocean.Pipeline.Interface;

import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Pipeline.Pipeline;

import java.io.IOException;

public interface MessageHandlerContext {
    Channel channel();
    Pipeline pipeline();
    void nextRead(Object msg) throws Throwable;
    void nextWrite(Object msg) throws Throwable;
    void nextExceptionHandler(Throwable t);
    void nextActive() throws Throwable;
    void nextInactive() throws Throwable;
}
