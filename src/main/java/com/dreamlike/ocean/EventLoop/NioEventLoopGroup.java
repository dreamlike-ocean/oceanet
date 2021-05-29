package com.dreamlike.ocean.EventLoop;

import java.util.concurrent.atomic.AtomicInteger;

public class NioEventLoopGroup {

    private NioEventLoop[]  nioEventLoops;
    private AtomicInteger choose;

    public NioEventLoopGroup(int size) {
        nioEventLoops = new NioEventLoop[size];
        for (int i = 0; i < nioEventLoops.length; i++) {
            nioEventLoops[i] = new NioEventLoop();
        }
        choose = new AtomicInteger();
    }
    public void start(){
        for (NioEventLoop loop : nioEventLoops) {
            loop.start();
        }
    }

    public NioEventLoop getEventLoop(){
        return nioEventLoops[choose.getAndIncrement() % nioEventLoops.length];
    }
}
