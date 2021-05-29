package com.dreamlike.ocean.Channel;

import com.dreamlike.ocean.EventLoop.NioEventLoop;
import com.dreamlike.ocean.EventLoop.NioEventLoopGroup;
import com.dreamlike.ocean.Handler.ChannelReadHandler;
import com.dreamlike.ocean.Handler.Impl.ChannelInitHandler;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServerChannel extends Channel{

    public NioServerChannel(NioEventLoop nioEventLoop) throws IOException {
        super(null, nioEventLoop,ServerSocketChannel.open());
        javaChannel.configureBlocking(false);
    }

    @Override
    public void connect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register() {
        eventLoop.registerInterest(this, SelectionKey.OP_ACCEPT);
    }

    @Override
    protected Object read0() throws IOException {
        return ((ServerSocketChannel) javaChannel).accept();
    }

    @Override
    public void write(Object msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServerSocketChannel javaChanel() {
        return (ServerSocketChannel) javaChannel;
    }
    public void listen(int port) throws IOException {
        javaChanel().bind(new InetSocketAddress(port));
    }

    public static class AcceptorHandler implements ChannelReadHandler{
        private NioEventLoopGroup worker;
        private NioServerChannel parentChannel;
        private ChannelInitHandler channelInitHandler;

        public AcceptorHandler(NioEventLoopGroup worker, NioServerChannel parentChannel, ChannelInitHandler channelInitHandler) {
            this.worker = worker;
            this.parentChannel = parentChannel;
            this.channelInitHandler = channelInitHandler;
        }

        @Override
        public void read(MessageHandlerContext mhc, Object msg) throws Throwable {
            SocketChannel socketChannel = (SocketChannel) msg;
            socketChannel.configureBlocking(false);
            NioEventLoop selectedEventloop = worker.getEventLoop();
            NioByteChannel channel = new NioByteChannel(parentChannel, selectedEventloop, socketChannel);
            channel.register();
            channel.pipeline.addLast(channelInitHandler);
            mhc.nextRead(channel);
        }
    }
}
