package com.dreamlike.ocean.Util;

import com.dreamlike.ocean.Channel.Channel;

public class EventLoopUtil {

    public static boolean inEventLoop(Channel channel){
        return channel.eventLoop() == Thread.currentThread();
    }

    public static void runOnContext(Channel channel,Runnable runnable){
        channel.eventLoop().submit(runnable);
    }

    public static void runOnContextInstant(Channel channel,Runnable runnable){
        if (inEventLoop(channel)){
            runnable.run();
            return;
        }
        runOnContext(channel, runnable);
    }
}
