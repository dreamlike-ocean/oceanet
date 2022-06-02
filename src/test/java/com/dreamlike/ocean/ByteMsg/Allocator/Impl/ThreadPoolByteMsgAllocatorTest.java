package com.dreamlike.ocean.ByteMsg.Allocator.Impl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ThreadPoolByteMsgAllocatorTest {

    ThreadPoolByteMsgAllocator allocator;

    @BeforeMethod
    public void setUp() {
        allocator = new ThreadPoolByteMsgAllocator(SharedByteMsgAllocator.DEFAULT_INSTANT, 4 * 1024, 128);
    }

    @AfterMethod
    public void tearDown() {
        allocator = null;
    }

    @Test
    public void testNormalAllocate() {
        var msg = allocator.allocate(1);
        assertEquals(msg.getOffset(), 0);
        assertEquals(msg.maxCapacity(), 128);
        msg.release();

        msg = allocator.allocate(129);
        assertEquals(msg.getOffset(), 0);
        assertEquals(msg.maxCapacity(), 256);

        var msg2 = allocator.allocate(224);
        assertEquals(msg2.getOffset(), 2);
        assertEquals(msg2.maxCapacity(), 256);

        msg.release();
        msg2.release();

        // 委托给SharedByteMsgAllocator分配
        msg = allocator.allocate(4097);
        assertTrue(msg.allocator() instanceof SharedByteMsgAllocator);
        msg.release();
    }

    @Test
    public void testReuse(){
        // msg:    |msg1|msg2|msg3     |msg4               |
        // chunk:  |    |    |    |    |    |    |    |    |
        var msg = allocator.allocate(128);
        var msg2 = allocator.allocate(128);
        var msg3 = allocator.allocate(256);
        var msg4 = allocator.allocate(512);

        assertEquals(msg.getOffset(), 0);
        assertEquals(msg2.getOffset(), 1);
        assertEquals(msg3.getOffset(), 2);
        assertEquals(msg4.getOffset(), 4);

        msg.release();
        msg2.release();

        // 重用chunk 0,1
        msg = allocator.allocate(256);
        assertEquals(msg.getOffset(), 0);
        msg.release();

        msg3.release();
        msg4.release();
    }

    @Test
    public void testChunkSize() {
        assertEquals(allocator.chunkSize(), 128);
    }
}