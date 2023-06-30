package pl.zielona_baza.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class AmazonS3Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Util.class);
    private static final String BUCKET_NAME;

    static {
        BUCKET_NAME = System.getenv("AWS_BUCKET_NAME");
    }

    public static List<String> listFolder(String folderName) {
        S3Client client = S3Client.builder().build();
        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .bucket(BUCKET_NAME)
                .prefix(folderName)
                .build();

        ListObjectsResponse response = client.listObjects(listRequest);

        List<S3Object> contents = response.contents();
        ListIterator<S3Object> s3ObjectListIterator = contents.listIterator();
        List<String> listKeys = new ArrayList<>();

        while (s3ObjectListIterator.hasNext()) {
            S3Object object = s3ObjectListIterator.next();
            listKeys.add(object.key());
        }

        return listKeys;
    }

    public static void uploadFile(String folderName, String fileName, InputStream inputStream) {
        S3Client client = S3Client.builder().build();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(folderName + "/" + fileName)
                .acl("public-read")
                .build();

        try(inputStream) {
            int contentLength = inputStream.available();
            client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        } catch (IOException ex) {
            LOGGER.error("Could not upload file to Amazon S3", ex);
        }
    }

    public static void deleteFile(String fileName) {
        S3Client client = S3Client.builder().build();

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(fileName)
                .build();

        client.deleteObject(request);
    }

    public static void removeFolder(String folderName) {
        S3Client client = S3Client.builder().build();
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(BUCKET_NAME)
                .prefix(folderName)
                .build();

        ListObjectsResponse response = client.listObjects(request);

        List<S3Object> contents = response.contents();

        for (S3Object object : contents) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(object.key())
                    .build();
            client.deleteObject(deleteRequest);
        }
    }














}
