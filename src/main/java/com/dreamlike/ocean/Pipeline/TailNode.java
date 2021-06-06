package com.dreamlike.ocean.Pipeline;

import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Exception.UnCaughtException;
import com.dreamlike.ocean.Handler.ChannelHandler;
import com.dreamlike.ocean.Handler.ChannelReadHandler;
import com.dreamlike.ocean.Handler.ChannelWriteHandler;
import com.dreamlike.ocean.Pipeline.Interface.HandlerNode;
import com.dreamlike.ocean.Pipeline.Interface.MessageHandlerContext;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayDeque;

public class TailNode extends HandlerNode {

    private int unflush;

    private ArrayDeque<ByteMsg> unflushMsg;

    private int maxSpin;



    public TailNode(Pipeline pipeline) {
        super(pipeline,new TailNodeHandler());
        unflush = 0;
        unflushMsg = new ArrayDeque<>();
        maxSpin = 64;
        ((TailNodeHandler) getHandler()).bindTailNode(this);
    }

    @Override
    public boolean isReadHandler() {
        return true;
    }

    @Override
    public boolean isWriteHandler() {
        return true;
    }

    @Override
    public ChannelHandler getHandler() {
        return channelHandler;
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public HandlerNode next() {
        return null;
    }



    @Override
    public HandlerNode pre() {
        return pre;
    }

    @Override
    public void setPre(HandlerNode handlerNode) {
        pre = handlerNode;
    }

    @Override
    public void setNext(HandlerNode handlerNode) {

    }

    public void flush() throws IOException {
        Channel channel = pipeline.getChannel();
        while (!unflushMsg.isEmpty()) {
            //单次
            int hasWrite = 0;
            ByteMsg wait = unflushMsg.peek();
            int spin = 0;
            while (spin < maxSpin) {
                int i = wait.writeToChannel(channel);
                hasWrite+=i;
                if (hasWrite == wait.size()){
                    break;
                }
                if (i == 0){
                    spin++;
                }
            }
            unflush -= hasWrite;
            //本次全部写出
            if (spin < maxSpin){
                wait.release();
                channel.eventLoop().removeInterest(channel, SelectionKey.OP_WRITE);
                unflushMsg.removeFirst();
                continue;
            }
            //自旋过度直接挂载写方法
            channel.eventLoop().addInterest(channel, SelectionKey.OP_WRITE);
            break;
        }
    }

    private static class TailNodeHandler implements ChannelReadHandler,ChannelWriteHandler{

        private TailNode tailNode;

        public void bindTailNode(TailNode node){
            tailNode = node;
        }

        @Override
        public void onException(MessageHandlerContext mhc, Throwable e) {
            throw new UnCaughtException(e);
        }

        @Override
        public void write(MessageHandlerContext mhc, Object msg) {
            if (msg instanceof ByteMsg){
                tailNode.unflushMsg.offer((ByteMsg) msg);
                tailNode.unflush += ((ByteMsg) msg).size();
                return;
            }

            throw new IllegalArgumentException("数据未转化为ByteMsg子类");
        }

        @Override
        public void read(MessageHandlerContext mhc, Object msg) {
            if (msg instanceof ByteMsg){
                ((ByteMsg) msg).release();
            }
            if (msg instanceof Channel){
                return;
            }
            throw new IllegalArgumentException("数据未消费");
        }

        @Override
        public void OnActive(MessageHandlerContext mhc) throws Throwable {

        }

        @Override
        public void onInactive(MessageHandlerContext mhc) throws Throwable {

        }
    }
}
