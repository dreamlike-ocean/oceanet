package com.dreamlike.ocean.ByteMsg;

import com.dreamlike.ocean.Channel.Channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UnpooledByteMsg extends AbstractByteMsg{

    private int count;
    public UnpooledByteMsg(int maxCapacity) {
        super(maxCapacity);
        count = 1;
    }
    public UnpooledByteMsg(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity());
        internByteBuffer = byteBuffer;
        count = 1;
    }

    @Override
    protected void resizeBuffer() {
        internByteBuffer = UnpooledByteMsgAllocator.get().allocate(maxCapacity() + 128 * count++).nioBuffer();
    }

    @Override
    public int writeToChannel(Channel channel) throws IOException {
        internByteBuffer.flip();
        int write = ((SocketChannel) channel.javaChanel()).write(internByteBuffer);
        //todo 写指针
        return write;
    }

    @Override
    public int readFromChannel(Channel channel,ByteMsg byteMsg) throws IOException {
        int read = ((SocketChannel) channel.javaChanel()).read(byteMsg.nioBuffer());
        writerIndex += read;
        return read;
    }

    @Override
    public ByteMsgAllocator allocator() {
        return UnpooledByteMsgAllocator.get();
    }

    @Override
    public String toString() {
        return new String(readbytes(internByteBuffer.position()));
    }
}
