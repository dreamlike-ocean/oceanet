package com.dreamlike.ocean.Handler;

import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;

public interface ChannelHandler {

    default void onException(MessageHandlerContext mhc, Throwable e){
        mhc.nextExceptionHandler(e);
    }

    default void onAdded(MessageHandlerContext mhc){};

    default void onRemove(MessageHandlerContext mhc){}

    default void OnActive(MessageHandlerContext mhc) throws Throwable{
        mhc.nextActive();
    }

    default void onInactive(MessageHandlerContext mhc) throws Throwable{
        mhc.nextInactive();
    }


}
