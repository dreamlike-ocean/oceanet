package com.dreamlike.ocean.Channel;

import com.dreamlike.ocean.ByteMsg.ByteMsg;
import com.dreamlike.ocean.ByteMsg.ByteMsgAllocator;
import com.dreamlike.ocean.EventLoop.NioEventLoop;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import static com.dreamlike.ocean.Util.EventLoopUtil.*;

public class NioByteChannel extends Channel{


    public NioByteChannel(Channel parentChannel, NioEventLoop nioEventLoop, SelectableChannel javaChannel) throws IOException {
        super(parentChannel, nioEventLoop, javaChannel);
    }

    @Override
    protected Object read0() throws IOException {
        ByteMsgAllocator allocator = eventLoop.getByteMsgAllocator();
        ByteMsg byteMsg = allocator.allocate(256);
        int readCount = byteMsg.readFromChannel(this,byteMsg);
        if (readCount < 0){
            this.close();
            return null;
        }
        if (readCount == 0){
            byteMsg.release();
            return null;
        }
        return byteMsg;
    }


    @Override
    public SocketChannel javaChanel() {
        return ((SocketChannel) javaChannel);
    }

    @Override
    public void connect() {
        if (!inEventLoop(this)){
            runOnContext(this,this::connect);
            return;
        }
        pipeline.active();
    }

    @Override
    public void register() {
        if (!inEventLoop(this)){
            runOnContext(this,this::register);
            return;
        }
        eventLoop.registerInterest(this, SelectionKey.OP_READ);
    }
}
