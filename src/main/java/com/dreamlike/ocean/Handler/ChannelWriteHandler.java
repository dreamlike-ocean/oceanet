package com.dreamlike.ocean.Handler;

import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;

public interface ChannelWriteHandler extends ChannelHandler{
   default void write(MessageHandlerContext mhc, Object msg) throws Throwable{
       mhc.nextWrite(msg);
   }
}
