package de.tum.i13.helper;

import java.util.Arrays;

public class HistogramHelper {
    public static int lookupRegion(int[] bins, int value) {
        int res = Arrays.binarySearch(bins, value);
        if(res >= 0) {
            return res;
        } else {
            int temp = ~res;
            if(temp >= bins.length) {
                return bins.length -1;
            } else {
                return temp;
            }
        }
    }
}
