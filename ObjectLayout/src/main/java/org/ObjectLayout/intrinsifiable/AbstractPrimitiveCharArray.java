/*
 * Written by Gil Tene and Martin Thompson, and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.ObjectLayout.intrinsifiable;

/**
 * This class contains the intrinsifiable portions of PrimitiveCharArray behavior. JDK implementations
 * that choose to intrinsify PrimitiveCharArray are expected to replace the implementation of this
 * base class.
 */

public abstract class AbstractPrimitiveCharArray extends PrimitiveArray {

    private final char[][] longAddressableElements; // Used to store elements at indexes above Integer.MAX_VALUE
    private final char[] intAddressableElements;

    protected final char[] _asArray() {
        if (getLength() > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Cannot make char[] from array with more than Integer.MAX_VALUE elements (" +
                            getLength() + ")");
        }
        return intAddressableElements;
    }

    protected char _get(final int index) {
        return intAddressableElements[index];
    }

    protected char _get(final long index) {
        if (index < Integer.MAX_VALUE) {
            return _get((int) index);
        }

        // Calculate index into long-addressable-only partitions:
        final long longIndex = (index - Integer.MAX_VALUE);
        final int partitionIndex = (int)(longIndex >>> MAX_EXTRA_PARTITION_SIZE_POW2_EXPONENT);
        final int partitionOffset = (int)longIndex & PARTITION_MASK;

        return longAddressableElements[partitionIndex][partitionOffset];
    }

    protected void _set(final int index, final char value) {
        intAddressableElements[index] = value;
    }

    protected void _set(final long index, final char value) {
        if (index < Integer.MAX_VALUE) {
            _set((int) index, value);
        }

        // Calculate index into long-addressable-only partitions:
        final long longIndex = (index - Integer.MAX_VALUE);
        final int partitionIndex = (int)(longIndex >>> MAX_EXTRA_PARTITION_SIZE_POW2_EXPONENT);
        final int partitionOffset = (int)longIndex & PARTITION_MASK;

        longAddressableElements[partitionIndex][partitionOffset] = value;
    }
    
    protected AbstractPrimitiveCharArray() {
        intAddressableElements = (char[]) createIntAddressableElements(char.class);
        longAddressableElements = (char[][]) createLongAddressableElements(char.class);
    }

    protected AbstractPrimitiveCharArray(AbstractPrimitiveCharArray sourceArray) {
        intAddressableElements = sourceArray.intAddressableElements.clone();
        int numLongAddressablePartitions = sourceArray.longAddressableElements.length;
        longAddressableElements = new char[numLongAddressablePartitions][];
        for (int i = 0; i < numLongAddressablePartitions; i++) {
            longAddressableElements[i] = sourceArray.longAddressableElements[i].clone();
        }
    }
}