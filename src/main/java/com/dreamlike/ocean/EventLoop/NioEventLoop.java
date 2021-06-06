package com.dreamlike.ocean.EventLoop;

import com.dreamlike.ocean.ByteMsg.Allocator.ByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Allocator.Impl.SharedByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Allocator.Impl.ThreadPoolByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Allocator.Impl.UnpooledByteMsgAllocator;
import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Channel.NioByteChannel;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NioEventLoop extends Thread{

    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    private Selector selector;
    private AtomicBoolean isClose;
    private Queue<Runnable> workQueue;
    private PriorityBlockingQueue<ScheduleTask> scheduleTasks;
    private AtomicBoolean wakeUpFlag;

    private ByteMsgAllocator byteMsgAllocator;


    public NioEventLoop(){
        try {
            selector = Selector.open();
            isClose = new AtomicBoolean(false);
            workQueue = new ConcurrentLinkedQueue<>();
            scheduleTasks = new PriorityBlockingQueue<>(64,Comparator.comparingLong(ScheduleTask::getStartTime));
            setName("nioEventLoop:"+atomicInteger.getAndIncrement());
            wakeUpFlag = new AtomicBoolean(true);
            byteMsgAllocator = ThreadPoolByteMsgAllocator.DEFAULT_INSTANT;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!isClose.get()) {
            for (;;) {
               try {
                   //定时任务要到期
                   if (!scheduleTasks.isEmpty() && scheduleTasks.peek().getStartTime() - System.currentTimeMillis() <= 5) {
                       ScheduleTask scheduleTask = scheduleTasks.poll();
                       wakeUpFlag.compareAndSet(true, false);
                       long waitTime = scheduleTask.startTime - System.currentTimeMillis();
                       //考虑过时的任务
                       if (waitTime > 0){
                           selector.select(waitTime);
                           wakeUpFlag.compareAndSet(false, true);
                       }
                       scheduleTask.runnable.run();
                       break;
                   }
                   if (!workQueue.isEmpty()){
                       workQueue.poll().run();
                       break;
                   }
                   processKeys();
               }catch (IOException e){
                   //todo IOException
               }
            }
        }
    }



    public boolean submit(Runnable runnable){
        boolean success = workQueue.add(runnable);
        if (success && !wakeUpFlag.get()){
            wakeUp();
        }
        return success;
    }
    public boolean scheduleTask(Runnable runnable, long period, TimeUnit timeUnit){
        ScheduleTask scheduleTask = new ScheduleTask(runnable, timeUnit.toMillis(period));
        boolean offer = scheduleTasks.offer(scheduleTask);
        if(offer && !wakeUpFlag.get()){
            wakeUp();
        }
        return offer;
    }

    private void processKeys() throws IOException {
        wakeUpFlag.compareAndSet(true, false);
        if (!scheduleTasks.isEmpty()){
            selector.select(scheduleTasks.peek().getStartTime() - System.currentTimeMillis());
        }else {
            selector.select();
        }
        //被外部唤醒的
        if (hasTask()){
            return;
        }
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()){
            SelectionKey selectionKey = iterator.next();
            Channel channel = (Channel) selectionKey.attachment();
            int readyOps = selectionKey.readyOps();
            if ((readyOps & SelectionKey.OP_CONNECT) != 0){
                int ops = selectionKey.interestOps();
                ops &= ~SelectionKey.OP_CONNECT;
                selectionKey.interestOps(ops);
                ((SocketChannel) selectionKey.channel()).finishConnect();
                ((NioByteChannel)channel).connect();
            }
            if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                channel.flush();
            }
            if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
                channel.read();
            }
            iterator.remove();
        }

    }

    public void registerInterest(Channel channel,int interestOp){
        if (Thread.currentThread() != this){
                this.submit(() -> registerInterest(channel,interestOp));
                return;
        }
        try {
            SelectableChannel selectableChannel = channel.javaChanel();
            selectableChannel.register(selector, interestOp,channel);
        } catch (ClosedChannelException e) {
            channel.catchException(e);
        }
    }

    public void addInterest(Channel channel,int interestOp){
        if (!channel.javaChanel().isRegistered()) {
            registerInterest(channel, interestOp);
            return;
        }
        if (Thread.currentThread() != this){
            this.submit(() -> addInterest(channel,interestOp));
            return;
        }
        SelectionKey selectionKey = channel.javaChanel().keyFor(selector);
        selectionKey.interestOps(selectionKey.interestOps()|interestOp);
    }

    public void removeInterest(Channel channel,int interestOp){
        if (!channel.javaChanel().isRegistered()) {
            return;
        }
        if (Thread.currentThread() != this){
            this.submit(() -> removeInterest(channel,interestOp));
            return;
        }
        SelectionKey selectionKey = channel.javaChanel().keyFor(selector);
        selectionKey.interestOps(selectionKey.interestOps()&(~interestOp));
    }


    private void wakeUp(){
        selector.wakeup();
        wakeUpFlag.compareAndSet(false, true);
    }

    private boolean hasTask(){
        return !workQueue.isEmpty() || !scheduleTasks.isEmpty();
    }

    public void close(){
       isClose.compareAndSet(false, true);
    }

    public ByteMsgAllocator getByteMsgAllocator() {
        return byteMsgAllocator;
    }

    private static class ScheduleTask {
        private Runnable runnable;
        private long startTime;

        public ScheduleTask(Runnable runnable, long delay) {
            this.runnable = runnable;
            this.startTime = delay + System.currentTimeMillis();
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public long getStartTime() {
            return startTime;
        }

    }
}
