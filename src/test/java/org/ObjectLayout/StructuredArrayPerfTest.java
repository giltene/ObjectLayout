/*
 * Copyright 2013 Gil Tene
 * Copyright 2012, 2013 Martin Thompson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ObjectLayout;

import org.junit.Test;

import static java.lang.Long.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StructuredArrayPerfTest {
    StructuredArray<MockStructure> array;
    EncapsulatedArray encapsulatedArray;

    class EncapsulatedArray {
        final MockStructure[] array;

        EncapsulatedArray(int length) {
            array = new MockStructure[length];
            for (int i = 0; i < array.length; i++) {
                array[i] = new MockStructure(i, i*2);
            }
        }

        MockStructure get(final int index) {
            return array[index];
        }

        int getLength() {
            return array.length;
        }
    }

    long loopSumTest() {
        long sum = 0;
        for (int i = 0 ; i < array.getLength(); i++) {
//            if (array.get(i) != null)
//                sum++;
            sum += array.get(i).getTestValue();
        }
        return sum;
    }

    long loopEncapsulatedArraySumTest() {
        long sum = 0;
        for (int i = 0 ; i < encapsulatedArray.getLength(); i++) {
            sum += encapsulatedArray.get(i).getTestValue();
        }
        return sum;
    }

    @Test
    public void shouldConstructArrayOfGivenLengthAndInitValues() throws NoSuchMethodException {
        final Class[] initArgTypes = {long.class, long.class};
        final long expectedIndex = 4L;
        final long expectedValue = 777L;
        final int length = 10000000;

        final ConstructorAndArgsLocator<MockStructure> constructorAndArgsLocator =
                new DefaultMockConstructorAndArgsLocator();


        array = StructuredArray.newInstance(constructorAndArgsLocator, length);
        encapsulatedArray = new EncapsulatedArray(length);

        while (true) {
            long startTime1 = System.nanoTime();
            long sum1 = loopSumTest();
            long endTime1 = System.nanoTime();
            double loopsPerSec1 = 1000 * (double) length / (endTime1 - startTime1);


            long startTime2 = System.nanoTime();
            long sum2 = loopEncapsulatedArraySumTest();
            long endTime2 = System.nanoTime();
            double loopsPerSec2 = 1000 * (double) length / (endTime2 - startTime2);

            System.out.println("sum1 = " + sum1 + " (" + loopsPerSec1 + "M),  sum2 = " + sum2 + " (" + loopsPerSec2 + "M)");
        }
    }

    public static void main(String[] args) {
        try {
            StructuredArrayPerfTest test = new StructuredArrayPerfTest();
            test.shouldConstructArrayOfGivenLengthAndInitValues();
        } catch (Exception ex) {
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Test support below
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void assertCorrectFixedInitialisation(final long expectedIndex, final long expectedValue, final long[] lengths,
                                                  final StructuredArray<MockStructure> array) {
        for (int i = 0; i < lengths.length; i++) {
            assertThat(valueOf(array.getLengths()[i]), is(valueOf(lengths[i])));
        }
        assertTrue(array.getElementClass() == MockStructure.class);

        final long[] cursors = new long[lengths.length];
        final long totalElementCount = array.getTotalElementCount();
        long elementCountToCursor = 0;

        while (elementCountToCursor < totalElementCount) {
            // Check element at cursors:
            MockStructure mockStructure = array.get(cursors);
            assertThat(valueOf(mockStructure.getIndex()), is(valueOf(expectedIndex)));
            assertThat(valueOf(mockStructure.getTestValue()), is(valueOf(expectedValue)));

            // Increment cursors from inner-most dimension out:
            for (int cursorDimension = cursors.length - 1; cursorDimension >= 0; cursorDimension--) {
                if ((++cursors[cursorDimension]) < lengths[cursorDimension])
                    break;
                // This dimension wrapped. Reset to zero and continue to one dimension higher
                cursors[cursorDimension] = 0;
            }
            elementCountToCursor++;
        }
    }

    private void assertCorrectVariableInitialisation(final long[] lengths,
                                             final StructuredArray<MockStructure> array) {
        for (int i = 0; i < lengths.length; i++) {
            assertThat(valueOf(array.getLengths()[i]), is(valueOf(lengths[i])));
        }
        assertTrue(array.getElementClass() == MockStructure.class);

        final long[] cursors = new long[lengths.length];
        final long totalElementCount = array.getTotalElementCount();
        long elementCountToCursor = 0;

        while (elementCountToCursor < totalElementCount) {
            // Check element at cursors:
            MockStructure mockStructure = array.get(cursors);

            long indexSum = 0;
            String cursorsString = "";
            for (long index : cursors) {
                indexSum += index;
                cursorsString += index + ",";
            }

            assertThat("elementCountToCursor: " + elementCountToCursor + " cursors: " + cursorsString,
                    valueOf(mockStructure.getIndex()), is(valueOf(indexSum)));
            assertThat("elementCountToCursor: " + elementCountToCursor + " cursors: " + cursorsString,
                    valueOf(mockStructure.getTestValue()), is(valueOf(indexSum * 2)));

            // Increment cursors from inner-most dimension out:
            for (int cursorDimension = cursors.length - 1; cursorDimension >= 0; cursorDimension--) {
                if ((++cursors[cursorDimension]) < lengths[cursorDimension])
                    break;
                // This dimension wrapped. Reset to zero and continue to one dimension higher
                cursors[cursorDimension] = 0;
            }
            elementCountToCursor++;
        }
    }

    private void initValues(final long[] lengths, final StructuredArray<MockStructure> array) {
        final long[] cursors = new long[lengths.length];
        final long totalElementCount = array.getTotalElementCount();
        long elementCountToCursor = 0;

        while (elementCountToCursor < totalElementCount) {
            // Check element at cursors:
            MockStructure mockStructure = array.get(cursors);

            long indexSum = 0;
            for (long index : cursors) {
                indexSum += index;
            }

            mockStructure.setIndex(indexSum);
            mockStructure.setTestValue(indexSum * 2);

            // Increment cursors from inner-most dimension out:
            for (int cursorDimension = cursors.length - 1; cursorDimension >= 0; cursorDimension--) {
                if ((++cursors[cursorDimension]) < lengths[cursorDimension])
                    break;
                // This dimension wrapped. Reset to zero and continue to one dimension higher
                cursors[cursorDimension] = 0;
            }
            elementCountToCursor++;
        }
    }

    public static class MockStructure {

        private long index = -1;
        private long testValue = Long.MIN_VALUE;

        public MockStructure() {
        }

        public MockStructure(final long index, final long testValue) {
            this.index = index;
            this.testValue = testValue;
        }

        public MockStructure(MockStructure src) {
            this.index = src.index;
            this.testValue = src.testValue;
        }

        public long getIndex() {
            return index;
        }

        public void setIndex(final long index) {
            this.index = index;
        }

        public long getTestValue() {
            return testValue;
        }

        public void setTestValue(final long testValue) {
            this.testValue = testValue;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final MockStructure that = (MockStructure)o;

            return index == that.index && testValue == that.testValue;
        }

        public int hashCode() {
            int result = (int)(index ^ (index >>> 32));
            result = 31 * result + (int)(testValue ^ (testValue >>> 32));
            return result;
        }

        public String toString() {
            return "MockStructure{" +
                    "index=" + index +
                    ", testValue=" + testValue +
                    '}';
        }
    }

    public static class MockStructureWithFinalField {

        private final int value = 888;
    }

    private static class DefaultMockConstructorAndArgsLocator extends ConstructorAndArgsLocator<MockStructure> {

        private final Class[] argsTypes = {Long.TYPE, Long.TYPE};

        public DefaultMockConstructorAndArgsLocator() throws NoSuchMethodException {
            super(MockStructure.class);
        }

        public ConstructorAndArgs<MockStructure> getForIndexes(long indexes[]) throws NoSuchMethodException {
            long indexSum = 0;
            for (long index : indexes) {
                indexSum += index;
            }
            Object[] args = {indexSum, indexSum * 2};
            // We could do this much more efficiently with atomic caching of a single allocated ConstructorAndArgs,
            // as SingleDimensionalCopyConstructorAndArgsLocator does, but no need to put in the effort in a test...
            return new ConstructorAndArgs<MockStructure>(MockStructure.class.getConstructor(argsTypes), args);
        }

    }
}
