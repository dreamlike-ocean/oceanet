package com.dreamlike.ocean.Util;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.dreamlike.ocean.Util.AllocatorUtil.isPowerOf2;
import static com.dreamlike.ocean.Util.AllocatorUtil.roundUp;
import static org.testng.Assert.*;

public class AllocatorUtilTest {

    @Test
    public void testIsPowerOf2() {
        assertFalse(isPowerOf2(-1));
        assertFalse(isPowerOf2(0));
        assertTrue(isPowerOf2(1));
        assertTrue(isPowerOf2(2));
        assertFalse(isPowerOf2(3));
        assertTrue(isPowerOf2(4));
        assertTrue(isPowerOf2(128));
    }

    @Test
    public void testRoundUp() {
        Assert.expectThrows(IllegalArgumentException.class, () -> roundUp(-1));
        Assert.expectThrows(IllegalArgumentException.class, () -> roundUp((1 << 30) + 1));

        assertEquals(roundUp(0), 1);
        assertEquals(roundUp(1), 1);
        assertEquals(roundUp(2), 2);
        assertEquals(roundUp(3), 4);
        assertEquals(roundUp(5), 8);
        assertEquals(roundUp(1 << 30), 1 << 30);
    }
}