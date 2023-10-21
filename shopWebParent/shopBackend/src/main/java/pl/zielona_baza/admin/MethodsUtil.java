package pl.zielona_baza.admin;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
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

    public static boolean isImage(MultipartFile file)
    {
        try {
            return ImageIO.read(file.getInputStream()) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
