package com.dreamlike.ocean.Handler.Impl;

import com.dreamlike.ocean.Handler.ChannelHandler;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;
import com.dreamlike.ocean.Pipeline.Pipeline;

public abstract class ChannelInitHandler implements ChannelHandler {
    public abstract void init(Pipeline pipeline);

    @Override
    public void onAdded(MessageHandlerContext mhc) {
        init(mhc.pipeline());
        mhc.pipeline().removeHandler(this);
    }
}
