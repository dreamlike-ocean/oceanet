package com.dreamlike.ocean.ByteMsg;

import com.dreamlike.ocean.Exception.ByteMsgOverflowBound;

import java.nio.ByteBuffer;

public abstract class AbstractByteMsg implements ByteMsg {
    protected int readerIndex;
    protected int writerIndex;
    protected int maxCapacity;
    ByteBuffer internByteBuffer;
    protected static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(0);

    public AbstractByteMsg(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        internByteBuffer = EMPTY_BYTEBUFFER;
        readerIndex = writerIndex = 0;
    }

    @Override
    public int size() {
        return writerIndex;
    }

    @Override
    public void release() {
       allocator().release(this);
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    @Override
    public byte[] readbytes(int length) {
        if (readCheckBound(length)){
            byte[] bytes = new byte[length];
            int old = internByteBuffer.position();
            internByteBuffer
                    .flip()
                    .position(readerIndex)
                    .get(bytes)
                    .compact()
                    .position(old);
            readerIndex += length;
            return bytes;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public char readChar() {
        if (readCheckBound(2)) {
            char res = internByteBuffer.getChar(readerIndex++);
            readerIndex += 2;
            return res;
        }
        throw new ByteMsgOverflowBound();
    }

    @Override
    public short readShort() {
        if (!readCheckBound(2)){
            throw new ByteMsgOverflowBound();
        }
        short res = internByteBuffer.getShort(readerIndex);
        readerIndex+=2;
        return res;
    }

    @Override
    public int readInt() {
        if (!readCheckBound(4)){
            throw new ByteMsgOverflowBound();
        }
        int res = internByteBuffer.getInt(readerIndex);
        readerIndex+=4;
        return res;
    }

    @Override
    public long readLong() {
        if (!readCheckBound(8)){
            throw new ByteMsgOverflowBound();
        }
        long res = internByteBuffer.getLong(readerIndex);
        readerIndex+=8;
        return res;
    }


    @Override
    public double readDouble() {
        if (!readCheckBound(8)){
            throw new ByteMsgOverflowBound();
        }
        double res = internByteBuffer.getDouble(readerIndex);
        readerIndex += 8;
        return res;
    }

    @Override
    public float readFloat() {
        if (!readCheckBound(4)){
            throw new ByteMsgOverflowBound();
        }
        float res = internByteBuffer.getFloat(readerIndex);
        readerIndex += 4;
        return res;
    }

    @Override
    public ByteMsg writByte(byte b) {
        if (!writeCheckBound(1)){
            resizeBuffer();
        }
        internByteBuffer.put(b);
        writerIndex++;
        return this;
    }


    protected abstract void resizeBuffer();
//        maxCapacity = maxCapacity + 128;
//        ByteBuffer tar = allocator().allocate(maxCapacity).internByteBuffer;
//        ByteBuffer src = this.internByteBuffer;
//        src.position(0).limit(src.capacity());
//        tar.position(0).limit(tar.capacity());
//        tar.put(src);
//        tar.clear();
//        maxCapacity += 128;
//        this.internByteBuffer = tar;
//        allocator().release();


    @Override
    public ByteMsg writeChar(char c) {
        while (!writeCheckBound(2)){
            resizeBuffer();
        }
        internByteBuffer.putChar(c);
        writerIndex+=2;
        return this;
    }

    @Override
    public ByteMsg writeShort(short s) {
        while (!writeCheckBound(2)){
            resizeBuffer();
        }
        internByteBuffer.putShort(s);
        writerIndex += 2;
        return this;
    }

    @Override
    public ByteMsg writeInt(int i) {
        while (!writeCheckBound(4)){
            resizeBuffer();
        }
        internByteBuffer.putInt(i);
        writerIndex += 4;
        return this;
    }

    @Override
    public ByteMsg writeLong(long l) {
        while (!writeCheckBound(8)){
            resizeBuffer();
        }
        internByteBuffer.putLong(l);
        writerIndex += 8;
        return this;
    }

    @Override
    public ByteMsg writeDouble(double d) {
        while (!writeCheckBound(8)){
            resizeBuffer();
        }
        internByteBuffer.putDouble(d);
        writerIndex += 8;
        return this;
    }

    @Override
    public ByteMsg writeFloat(float f) {
        while (!writeCheckBound(4)){
            resizeBuffer();
        }
        internByteBuffer.putFloat(f);
        writerIndex += 4;
        return this;
    }

    @Override
    public ByteMsg writeBytes(byte[] bytes) {
        while (!writeCheckBound(bytes.length)){
            resizeBuffer();
        }
        internByteBuffer.put(bytes);
        writerIndex += bytes.length;
        return this;
    }

    // 0 1 2 3
    private boolean readCheckBound(int length){
        return readerIndex + length <= writerIndex;
    }
    // 0 1 2 3 4 max = 5
    private boolean writeCheckBound(int length){
        return writerIndex + length <=  maxCapacity;
    }

    @Override
    public ByteBuffer nioBuffer() {
        return internByteBuffer;
    }
}
