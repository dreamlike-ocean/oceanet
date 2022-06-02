package com.dreamlike.ocean.Util;

public class AllocatorUtil {
    /**
     * Test if <code>x<code/> is a power of 2.
     *
     * @param x the number to test
     * @return <code>true</code> if <code>x<code/> is a power of 2
     */
    public static boolean isPowerOf2(int x) {
        return x > 0 && Integer.bitCount(x) == 1;
    }

    /**
     * Find the closest power of 2 that is not smaller than the
     * <code>size<code/>.
     *
     * @param size the size
     * @return the closest power of 2
     */
    public static int roundUp(int size) {
        if (size < 0) // Check for edge case
            throw new IllegalArgumentException("size must be non-negative");
        if (size > (1 << 30)) // Check for edge case
            throw new IllegalArgumentException("size must be no more than 2^31");
        if (isPowerOf2(size)) // return if it is already a power of 2
            return size;
        size |= size >> 1;
        size |= size >> 2;
        size |= size >> 4;
        size |= size >> 8;
        size |= size >> 16;
        return size + 1;
    }

    public static int leftLeaf(int index) {
        return index * 2 + 1;
    }

    public static int rightLeaf(int index) {
        return index * 2 + 2;
    }

    public static int parent(int index) {
        return (((index) + 1) / 2 - 1);
    }
}
