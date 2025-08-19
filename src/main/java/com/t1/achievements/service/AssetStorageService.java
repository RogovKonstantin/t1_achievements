package com.t1.achievements.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AssetStorageService {

    private final MinioClient minio;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void ensureBucket() throws Exception {
        boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    public ObjectWriteResponse uploadPng(byte[] bytes, String objectKey) throws Exception {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, bytes.length, -1)
                            .contentType("image/png")
                            .build());
        }
    }

    public byte[] generateSquarePng(int size, boolean black) throws IOException {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(black ? Color.BLACK : Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @Value("${minio.externalEndpoint}")
    private String externalEndpoint;

    public String presignedGet(String objectKey, Duration ttl) throws Exception {
        String url = minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry((int) ttl.toSeconds())
                        .build()
        );

        // меняем хост на внешний
        URI uri = URI.create(url);
        return externalEndpoint + uri.getRawPath() +
                (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
    }


    public ObjectWriteResponse upload(byte[] bytes, String objectKey, String contentType) throws Exception {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        }
    }
}
