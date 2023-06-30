package pl.zielona_baza.admin;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AmazonS3UtilTests {

    @Test
    public void testListFolder() {
        String folderName = "user-photos/1";
        AmazonS3Util.listFolder(folderName);
    }

    @Test
    public void testUploadFile() throws FileNotFoundException {
        String folderName = "test-upload";
        String fileName = "java.png";
        String filePath = "C:\\ZDJECIA\\" + fileName;

        InputStream inputStream = new FileInputStream(filePath);

        AmazonS3Util.uploadFile(folderName, fileName, inputStream);
    }

    @Test
    public void testDeleteFile() {
        String fileName = "test-upload/java.png";

        AmazonS3Util.deleteFile(fileName);
    }
}
