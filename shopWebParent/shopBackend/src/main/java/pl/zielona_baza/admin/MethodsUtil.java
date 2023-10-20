package pl.zielona_baza.admin;

import java.lang.reflect.Array;

public class MethodsUtil {
    public static void checkParallelArrays(Object... arrays) {
        if (arrays.length < 1) {
            return;
        }
        int expectedLength = Array.getLength(arrays[0]);
        for (int i=1; i<arrays.length; i++) {
            int length = Array.getLength(arrays[i]);
            if (length != expectedLength) {
                throw new IllegalArgumentException("Array %d doesn't have expected length".formatted(i));
            }
        }
    }
}
