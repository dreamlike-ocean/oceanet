package com.dreamlike.ocean.Pipeline;

import com.dreamlike.ocean.Handler.ChannelHandler;
import com.dreamlike.ocean.Handler.ChannelReadHandler;
import com.dreamlike.ocean.Handler.ChannelWriteHandler;
import com.dreamlike.ocean.Pipeline.Interface.HandlerNode;

public class PlainNode extends HandlerNode{

    private boolean read;
    private boolean write;

    public PlainNode(Pipeline pipeline, ChannelHandler channelHandler) {
        super(pipeline, channelHandler);
        read = channelHandler instanceof ChannelReadHandler;
        write  = channelHandler instanceof ChannelWriteHandler;
    }

    @Override
    public boolean isReadHandler() {
        return read;
    }

    @Override
    public boolean isWriteHandler() {
        return write;
    }
}
