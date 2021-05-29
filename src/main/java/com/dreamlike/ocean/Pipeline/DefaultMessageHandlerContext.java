package com.dreamlike.ocean.Pipeline;

import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Handler.ChannelReadHandler;
import com.dreamlike.ocean.Handler.ChannelWriteHandler;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;
import com.dreamlike.ocean.Pipeline.Interface.HandlerNode;

import java.io.IOException;

import static com.dreamlike.ocean.Util.EventLoopUtil.*;

public class DefaultMessageHandlerContext implements MessageHandlerContext {

    private Channel channel;

    private Pipeline pipeline;

    private HandlerNode now;

    private TailNode tail;

    private HeadNode head;

    public DefaultMessageHandlerContext(Channel channel, Pipeline pipeline, TailNode tail,HeadNode head) {
        this.channel = channel;
        this.pipeline = pipeline;
        this.tail = tail;
        this.head = head;
        now = head;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public Pipeline pipeline() {
        return pipeline;
    }

    @Override
    public void nextRead(Object msg) throws Throwable {
       if (!inEventLoop(channel)){
           runOnContext(channel, () -> {
               try {
                   nextRead(msg);
               } catch (Throwable e) {
                   pipeline.catchException(e);
               }
           });
           return;
       }
       if (!isRemoved()){
           now = getNextReadNode();
           ((ChannelReadHandler) now.getHandler())
                   .read(this,msg);
       }
    }

    @Override
    public void nextWrite(Object msg) throws Throwable {
        if (!inEventLoop(channel)){
            runOnContext(channel, () -> {
                try {
                    nextWrite(msg);
                } catch (Throwable throwable) {
                    pipeline.catchException(throwable);
                }
            });
            return;
        }
        if (!isRemoved()){
            now = getNextWriteNode();
            ((ChannelWriteHandler) now.getHandler())
                    .write(this,msg);
        }
    }

    @Override
    public void nextExceptionHandler(Throwable t) {
        if (!inEventLoop(channel)){
            runOnContext(channel, () -> nextExceptionHandler(t));
            return;
        }
        if (!isRemoved()){
            now = getNextNode();
            now.getHandler().onException(this,t);
        }

    }

    @Override
    public void nextActive() throws Throwable{
        if (!inEventLoop(channel)){
            runOnContext(channel, () -> {
                try {
                    nextActive();
                } catch (Throwable throwable) {
                    pipeline.catchException(throwable);
                }
            });
            return;
        }
        if (!isRemoved()){
            now = getNextNode();
            now.getHandler().OnActive(this);
        }
    }

    @Override
    public void nextInactive() throws Throwable{
        if (!inEventLoop(channel)){
            runOnContext(channel, () -> {
                try {
                    nextInactive();
                } catch (Throwable throwable) {
                    pipeline.catchException(throwable);
                }
            });
            return;
        }
        if (!isRemoved()){
            now = getNextNode();
            now.getHandler().onInactive(this);
        }
    }

    private boolean isRemoved(){
        HandlerNode it = head;
        while (it != now){
            //被移除了
            if (it == tail){
                return true;
            }
            it = it.next();
        }
        return false;
    }


    private HandlerNode getNextReadNode(){
        HandlerNode handlerNode = now.next();
        while (!handlerNode.isReadHandler()){
            handlerNode = handlerNode.next();
        }
        return handlerNode;
    }

    private HandlerNode getNextWriteNode(){
        HandlerNode handlerNode = now.next();
        while (!handlerNode.isWriteHandler()){
            handlerNode = handlerNode.next();
        }
        return handlerNode;
    }

    private HandlerNode getNextNode(){
        return now.next();
    }



}
