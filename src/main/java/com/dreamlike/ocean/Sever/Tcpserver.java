package com.dreamlike.ocean.Sever;

import com.dreamlike.ocean.Channel.NioServerChannel;
import com.dreamlike.ocean.EventLoop.NioEventLoopGroup;
import com.dreamlike.ocean.Handler.Impl.ChannelInitHandler;

import java.io.IOException;

public class Tcpserver {
    private NioEventLoopGroup connectGroup;
    private NioEventLoopGroup workerGroup;
    private ChannelInitHandler initHandler;

    public Tcpserver(NioEventLoopGroup connectGroup, NioEventLoopGroup workerGroup) {
        this.connectGroup = connectGroup;
        this.workerGroup = workerGroup;
    }
    public Tcpserver init(ChannelInitHandler channelInitHandler){
        this.initHandler = channelInitHandler;
        return this;
    }
    public void start(int port) throws IOException {
        connectGroup.start();
        workerGroup.start();
        NioServerChannel serverChannel = new NioServerChannel(connectGroup.getEventLoop());
        serverChannel.listen(port);

        serverChannel.register();
        serverChannel.getPipeline()
                .addLast(new NioServerChannel.AcceptorHandler(workerGroup, serverChannel,initHandler));
    }
}
