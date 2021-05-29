package com.dreamlike.ocean.Pipeline;

import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Handler.ChannelHandler;
import com.dreamlike.ocean.Handler.ChannelReadHandler;
import com.dreamlike.ocean.Handler.ChannelWriteHandler;
import com.dreamlike.ocean.Pipeline.Interface.HandlerNode;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;
import com.dreamlike.ocean.Util.EventLoopUtil;

import java.io.IOException;

import static com.dreamlike.ocean.Util.EventLoopUtil.*;

public class Pipeline {
    private Channel channel;
    private TailNode tailNode;
    private HeadNode headNode;

    public Pipeline(Channel channel) {
        this.channel = channel;
        tailNode = new TailNode(this);
        headNode = new HeadNode(this);
        tailNode.setPre(headNode);
        headNode.setNext(tailNode);

    }


    public void read(Object msg) throws Throwable {
        ((ChannelReadHandler) headNode.getHandler())
                .read(creatMessageHandlerContext(),msg);
    }

    public void active(){
        try {
            headNode.getHandler()
                    .OnActive(creatMessageHandlerContext());
        } catch (Throwable throwable) {
            catchException(throwable);
        }
    }

    public void inactive(){
        try {
            headNode.getHandler()
                    .onInactive(creatMessageHandlerContext());
        } catch (Throwable throwable) {
            catchException(throwable);
        }
    }


    public void catchException(Throwable throwable){
        headNode.getHandler().onException(creatMessageHandlerContext(), throwable);
    }

    public void write(Object msg){
        try {
            ((ChannelWriteHandler) headNode.getHandler())
                    .write(creatMessageHandlerContext(),msg);
        } catch (Throwable throwable) {
            catchException(throwable);
        }
    }

    public void flush() throws IOException {
        tailNode.flush();
    }

    public void removeHandler(ChannelHandler channelHandler){
        if (!inEventLoop(channel)){
            EventLoopUtil.runOnContext(channel, ()->removeHandler(channelHandler));
            return;
        }
        HandlerNode it = this.headNode;
        while (it.getHandler() != channelHandler){
            if (it == tailNode){
                return;
            }
            it = it.next();
        }
        it.pre().setNext(it.next());
        it.next().setPre(it.pre());
        it.setPre(null);
        it.setNext(null);
        it.getHandler().onRemove(creatMessageHandlerContext());
    }

    public void addLast(ChannelHandler channelHandler){
        if (!inEventLoop(channel)){
            EventLoopUtil.runOnContext(channel, () -> addLast(channelHandler));
            return;
        }
        PlainNode plainNode = new PlainNode(this, channelHandler);
        plainNode.setPre(tailNode.pre());
        tailNode.pre().setNext(plainNode);
        tailNode.setPre(plainNode);
        plainNode.setNext(tailNode);
        channelHandler.onAdded(creatMessageHandlerContext());
    }


    public Channel getChannel() {
        return channel;
    }
    private MessageHandlerContext creatMessageHandlerContext(){
        return new DefaultMessageHandlerContext(channel, this, tailNode,headNode);
    }


}
