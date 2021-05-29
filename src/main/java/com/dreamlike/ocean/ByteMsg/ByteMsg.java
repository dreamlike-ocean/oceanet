package com.dreamlike.ocean.ByteMsg;

import com.dreamlike.ocean.Channel.Channel;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface ByteMsg {

    /**
     * 若第一次没写完，第二次再必须接着上一次写
     * @param channel 写入的channel
     * @return 写了多少
     */
    int writeToChannel(Channel channel) throws IOException;

    int readFromChannel(Channel channel,ByteMsg byteMsg) throws IOException;

    int size();

    default void release(){
        allocator().release(this);
    }


    int maxCapacity();
    
    byte[] readbytes(int length);

    char readChar();

    short readShort();

    int readInt();

    long readLong();

    double readDouble();

    float readFloat();

    ByteMsg writByte(byte b);

    ByteMsg writeChar(char c);

    ByteMsg writeShort(short s);

    ByteMsg writeInt(int i);

    ByteMsg writeLong(long l);

    ByteMsg writeDouble(double d);

    ByteMsg writeFloat(float f);

    ByteMsg writeBytes(byte[] bytes);

    ByteMsgAllocator allocator();

    ByteBuffer nioBuffer();
}
