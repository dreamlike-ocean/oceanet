package com.dreamlike.ocean.ByteMsg.Msg.impl;

import com.dreamlike.ocean.ByteMsg.Allocator.ByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Allocator.Impl.UnpooledByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.AbstractByteMsg;
import com.dreamlike.ocean.ByteMsg.Msg.ByteMsg;
import com.dreamlike.ocean.ByteMsg.Msg.ExtendableByteMsg;
import com.dreamlike.ocean.Channel.Channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UnpoolByteMsg extends AbstractByteMsg implements ExtendableByteMsg {

    private int resizeCount;
    protected static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(0);

    public UnpoolByteMsg(int maxCapacity) {
        super(maxCapacity);
        internByteBuff = EMPTY_BYTEBUFFER;
        readIndex = writeIndex = 0;
    }

    public UnpoolByteMsg(ByteBuffer byteBuffer){
        this(byteBuffer.capacity());
        internByteBuff = byteBuffer;
    }

    @Override
    public void release() {
        internByteBuff = null;
        allocator().release(this);
    }


    @Override
    public ByteMsg writByte(byte b) {
        if (!writeCheckBound(1)){
            resize();
        }
        internByteBuff.put(b);
        writeIndex++;
        return this;
    }

    public void resize(){
        int resize = 128 * resizeCount++;
        internByteBuff.flip();
        internByteBuff = UnpooledByteMsgAllocator.get().allocate(maxCapacity() + resize).nioBuffer().put(internByteBuff);
        maxCapacity += resize;
    }
    private ByteBuffer nioBuffer(){
        return internByteBuff;
    }
    @Override
    public ByteMsgAllocator allocator() {
        return UnpooledByteMsgAllocator.get();
    }


    @Override
    public ByteMsg writeChar(char c) {
        while (!writeCheckBound(2)){
            resize();
        }
        internByteBuff.putChar(c);
        writeIndex +=2;
        return this;
    }

    @Override
    public ByteMsg writeShort(short s) {
        while (!writeCheckBound(2)){
            resize();
        }
        internByteBuff.putShort(s);
        writeIndex += 2;
        return this;
    }

    @Override
    public ByteMsg writeInt(int i) {
        while (!writeCheckBound(4)){
            resize();
        }
        internByteBuff.putInt(i);
        writeIndex += 4;
        return this;
    }

    @Override
    public ByteMsg writeLong(long l) {
        while (!writeCheckBound(8)){
            resize();
        }
        internByteBuff.putLong(l);
        writeIndex += 8;
        return this;
    }

    @Override
    public ByteMsg writeDouble(double d) {
        while (!writeCheckBound(8)){
            resize();
        }
        internByteBuff.putDouble(d);
        writeIndex += 8;
        return this;
    }

    @Override
    public ByteMsg writeFloat(float f) {
        while (!writeCheckBound(4)){
            resize();
        }
        internByteBuff.putFloat(f);
        writeIndex += 4;
        return this;
    }

    @Override
    public ByteMsg writeBytes(byte[] bytes) {
        while (!writeCheckBound(bytes.length)){
            resize();
        }
        internByteBuff.put(bytes);
        writeIndex += bytes.length;
        return this;
    }

    // 0 1 2 3 4 max = 5
    private boolean writeCheckBound(int length){
        return writeIndex + length <=  maxCapacity;
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

}
