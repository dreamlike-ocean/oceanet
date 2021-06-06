package com.dreamlike.ocean.ByteMsg.Msg.impl;

import com.dreamlike.ocean.ByteMsg.Allocator.ByteMsgAllocator;
import com.dreamlike.ocean.ByteMsg.Msg.AbstractByteMsg;
import com.dreamlike.ocean.Channel.Channel;
import com.dreamlike.ocean.Exception.ByteMsgOverflowBound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombinedFixedByteMsg extends AbstractByteMsg {
    protected List<FixedPoolByteMsg> internMsges;
    protected int readIndex,writeIndex;
    protected int readSize,writeSize;

    protected int channelWriteOutIndex;
    public CombinedFixedByteMsg(){
        super(0);
        readIndex = writeIndex = readSize = writeSize = 0;
        internMsges = new ArrayList<>();
        channelWriteOutIndex = 0;
    }
    public CombinedFixedByteMsg(List<FixedPoolByteMsg> fixedPoolByteMsgs) {
        super(fixedPoolByteMsgs.stream().mapToInt(FixedPoolByteMsg::size).sum());
        readIndex = writeIndex = readSize = writeSize = 0;
        internMsges = new ArrayList<>();
        channelWriteOutIndex = 0;
    }
    @Override
    public CombinedFixedByteMsg writByte(byte b) {
        byte[] bytes = {b};
        writeBytes(bytes);
        return this;
    }

    @Override
    public byte readByte() {
        return readbytes(1)[0];
    }

    @Override
    public char readChar() {
        byte[] readbytes = readbytes(2);
        char r = 0;
        for (byte readbyte : readbytes) {
            r <<= 8;
            r |= (readbyte & 0xFF);
        }
        return r;
    }

    @Override
    public CombinedFixedByteMsg writeChar(char c) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (c >> 8);
        bytes[1] = (byte) (c);
        writeBytes(bytes);
        return this;
    }

    @Override
    //short 有符号 char无符号 所以看起来写的一样却不能强转
    public short readShort() {
        byte[] readbytes = readbytes(2);
        short r = 0;
        for (byte readbyte : readbytes) {
            r <<= 8;
            r |= (readbyte & 0xFF);
        }
        return r;
    }

    @Override
    public CombinedFixedByteMsg writeShort(short s) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (s >> 8);
        bytes[1] = (byte) (s);
        writeBytes(bytes);
        return this;
    }

    @Override
    public int readInt(){
        byte[] b = readbytes(4);
        int r = 0;
        for (byte value : b) {
            r <<= 8;
            r |= (value & 0xFF);
        }
        return r;
    }

    @Override
    public CombinedFixedByteMsg writeInt(int a) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((a >> 24));
        bytes[1] = (byte) ((a >> 16)) ;
        bytes[2] = (byte) ((a >> 8));
        bytes[3] = (byte) (a);
        writeBytes(bytes);
        return this;
    }

    @Override
    public long readLong() {
        byte[] b = readbytes(8);
        long r = 0;
        for (byte value : b) {
            r <<= 8;
            r |= (value & 0xFF);
        }
        return r;
    }

    @Override
    public CombinedFixedByteMsg writeLong(long l) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (l >> (7-i)*8);
        }
        writeBytes(bytes);
        return this;
    }

    @Override
    public CombinedFixedByteMsg writeBytes(byte[] bytes) {
        int needToWrite = bytes.length;
        if (writeSize + needToWrite > maxCapacity){
            throw new ByteMsgOverflowBound();
        }
        return writeBytes0(bytes, needToWrite);
    }

    protected CombinedFixedByteMsg writeBytes0(byte[] bytes,int needToWrite){
        int offset = 0;
        while (needToWrite > 0){
            FixedPoolByteMsg now = internMsges.get(writeIndex);
            int realWrite = now.writeBytes(bytes, offset, needToWrite);
            if (realWrite != needToWrite){
                writeIndex++;
            }
            needToWrite -= realWrite;
            offset += realWrite;
        }
        writeSize += bytes.length;
        return this;
    }

    @Override
    public byte[] readbytes(int needToRead) {
        if (writeSize - readSize < needToRead) {
            throw new ByteMsgOverflowBound();
        }
        byte[] bytes = new byte[needToRead];
        int offset = 0;
        while (needToRead > 0){
            FixedPoolByteMsg now = internMsges.get(readIndex);
            int realRead = now.readBytes(bytes, offset, needToRead);
            if (realRead != needToRead){
                readIndex ++;
            }
            needToRead -= realRead;
            offset += realRead;
        }
        //0 19 36  -95 0 0 97 -88
        readSize += needToRead;
        return bytes;
    }

    @Override
    public CombinedFixedByteMsg writeDouble(double d) {
        return writeLong(Double.doubleToLongBits(d));
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public CombinedFixedByteMsg writeFloat(float f) {
        return writeInt(Float.floatToIntBits(f));
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public int size() {
        return writeSize;
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    @Override
    public ByteMsgAllocator allocator() {
        return null;
    }

    @Override
    public void release() {
        internMsges.forEach(FixedPoolByteMsg::release);
        internMsges = null;
    }


    @Override
    public int writeToChannel(Channel channel) throws IOException {
        int write = 0;
        while (channelWriteOutIndex < internMsges.size()) {
            FixedPoolByteMsg now = internMsges.get(channelWriteOutIndex);
            int nowWriteSize = now.writeToChannel(channel);
            write += nowWriteSize;
            if (now.size() > nowWriteSize){
                break;
            }
            channelWriteOutIndex++;
        }
        return write;
    }

    @Override
    public int readFromChannel(Channel channel) throws IOException {
        int read = 0;
        while (writeIndex < internMsges.size()){
            FixedPoolByteMsg msg = internMsges.get(writeIndex);
            int i = msg.readFromChannel(channel);
            read += i;
            if (i == 0){
                break;
            }
            writeIndex ++;
        }
        writeSize += read;
        return read;
    }

    public void addFixedPoolByteMsg(FixedPoolByteMsg fixedPoolByteMsg){
        internMsges.add(fixedPoolByteMsg);
        maxCapacity += fixedPoolByteMsg.maxCapacity();
    }



}
