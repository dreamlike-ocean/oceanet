package com.dreamlike.ocean.ByteMsg.Msg.impl;

import com.dreamlike.ocean.ByteMsg.Allocator.ByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.AbstractByteMsg;
import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Exception.ByteMsgOverflowBound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixedPoolByteMsg extends AbstractByteMsg {

    protected final ByteMsgAllocator allocator;
    private AtomicBoolean isRelease;

    public FixedPoolByteMsg(ByteBuffer interByteBuff, ByteMsgAllocator allocator) {
        super(interByteBuff);
        this.internByteBuff = interByteBuff;
        this.allocator = allocator;
        isRelease = new AtomicBoolean(false);
    }


    @Override
    public int writeToChannel(Channel channel) throws IOException {
        internByteBuff.flip();
        int write = ((SocketChannel) channel.javaChanel()).write(internByteBuff);
        if (write < size()){
            //此时没写完
            internByteBuff.compact();
        }
        return write;
    }

    @Override
    public int readFromChannel(Channel channel) throws IOException {
        int read = ((SocketChannel) channel.javaChanel()).read(internByteBuff);
        writeIndex += read;
        return read;
    }

    @Override
    public int size() {
        return writeIndex;
    }

    public int lastSize(){
        // 0 1 2 3   max = 4 writeIndex = 1 -> 3
        return maxCapacity - writeIndex;
    }

    public int readSize(){
        // 0 1 2 3 4
        return writeIndex - readIndex;
    }

    @Override
    public void release() {
        //防止多线程或者多次释放
        if (isRelease.compareAndSet(false, true)) {
            internByteBuff.clear();
            allocator.release(this);
        }
    }
    //先分配被某一线程持有后再修改
    public void hold(){
        isRelease.set(false);
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    @Override
    public ByteMsgAllocator allocator() {
        return allocator;
    }

    //从offset处开始 0 1 2 3    1+3=4
    public int writeBytes(byte[] src,int offset,int expectLength){
        if (offset + expectLength > src.length){
            throw new ArrayIndexOutOfBoundsException();
        }
        int lastSize = lastSize();
        int writeSize = Math.min(lastSize, expectLength);
        writeIndex+=writeSize;
        internByteBuff.put(src,offset,writeSize);
        return writeSize;
    }

    //
    public int readBytes(byte[] src,int offset,int expectLength){
        if (offset + expectLength > src.length){
            throw new ArrayIndexOutOfBoundsException();
        }
        int readSize = readSize();
        int byteReadSize = Math.min(readSize, expectLength);
        int old = internByteBuff.position();
        int limit = internByteBuff.limit();
        internByteBuff
                .flip()
                .position(readIndex)
                .get(src,offset,byteReadSize)
                .limit(limit)
                .position(old);
        readIndex += byteReadSize;
        return byteReadSize;
    }


    @Override
    public ByteMsg writByte(byte b) {
        if (writeCheckBound(1)){
            internByteBuff.put(b);
            writeIndex++;
            return this;
        }
        throw new ByteMsgOverflowBound();

    }


    @Override
    public ByteMsg writeChar(char c) {
        if (writeCheckBound(2)){
            internByteBuff.putChar(c);
            writeIndex +=2;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public ByteMsg writeShort(short s) {
        if (writeCheckBound(2)){
            internByteBuff.putShort(s);
            writeIndex += 2;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public ByteMsg writeInt(int i) {
        if (writeCheckBound(4)){
            internByteBuff.putInt(i);
            writeIndex += 4;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public ByteMsg writeLong(long l) {
        if (writeCheckBound(8)){
            internByteBuff.putLong(l);
            writeIndex += 8;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public ByteMsg writeDouble(double d) {
        if(writeCheckBound(8)){
            internByteBuff.putDouble(d);
            writeIndex += 8;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public ByteMsg writeFloat(float f) {
        if(writeCheckBound(4)){
            internByteBuff.putFloat(f);
            writeIndex += 4;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public ByteMsg writeBytes(byte[] bytes) {
        if(writeCheckBound(bytes.length)){
            internByteBuff.put(bytes);
            writeIndex += bytes.length;
            return this;
        }
        throw new ByteMsgOverflowBound();
    }

    private boolean writeCheckBound(int length){
        return writeIndex + length <=  maxCapacity;
    }

}
