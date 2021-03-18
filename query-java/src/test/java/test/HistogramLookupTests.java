package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.tum.i13.helper.HistogramHelper.lookupRegion;

public class HistogramLookupTests {

    private int testLookup(int value) {
        int[] bins = new int[] {3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45, 48, 51, 54, 57, 60};
        return lookupRegion(bins, value);
    }

    @Test
    public void testBS() {
        Assertions.assertEquals(19, testLookup(90));
        Assertions.assertEquals(0, testLookup(3));
        Assertions.assertEquals(0, testLookup(1));
        Assertions.assertEquals(1, testLookup(4));
        Assertions.assertEquals(19, testLookup(60));
        Assertions.assertEquals(19, testLookup(80));
    }
}
