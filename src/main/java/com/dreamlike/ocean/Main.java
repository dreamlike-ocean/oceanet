package com.dreamlike.ocean;

import com.dreamlike.ocean.ByteMsg.ByteMsg;
import com.dreamlike.ocean.ByteMsg.UnpooledByteMsgAllocator;
import com.dreamlike.ocean.EventLoop.NioEventLoop;
import com.dreamlike.ocean.EventLoop.NioEventLoopGroup;
import com.dreamlike.ocean.Handler.ChannelReadHandler;
import com.dreamlike.ocean.Handler.Impl.ChannelInitHandler;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;
import com.dreamlike.ocean.Pipeline.Pipeline;
import com.dreamlike.ocean.Sever.Tcpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        NioEventLoopGroup connect = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(1);
        Tcpserver tcpserver = new Tcpserver(connect, worker);
        tcpserver.init(new ChannelInitHandler() {
            @Override
            public void init(Pipeline pipeline) {
                pipeline.addLast(new ChannelReadHandler() {
                    @Override
                    public void read(MessageHandlerContext mhc, Object msg) throws Throwable {
                        mhc.channel().writeAndFlush(UnpooledByteMsgAllocator.get().allocate(1024).writeBytes("string".getBytes(StandardCharsets.UTF_8)));
                    }
                });
            }
        });
        tcpserver.start(4399);
        new Scanner(System.in).next();
    }

}
