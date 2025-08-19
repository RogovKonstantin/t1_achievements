package com.t1.achievements.service;

import com.t1.achievements.entity.Asset;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetStorageService {

    private final MinioClient minio;

    @Value("${minio.bucket}")
    private String defaultBucket;

    /**
     * Базовый публичный URL, по которому доступен MinIO через реверс-прокси.
     * Пример: http://10.10.146.200/minio
     */
    @Value("${minio.publicBaseUrl}")
    private String publicBaseUrl;

    @PostConstruct
    public void ensureBucket() throws Exception {
        boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(defaultBucket).build());
        if (!exists) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(defaultBucket).build());
        }
    }

    // ---- PUBLIC URL ----

    public String publicUrl(Asset a) {
        if (a == null) return null;
        String bucket = a.getBucket() != null ? a.getBucket() : defaultBucket;
        return buildPublicUrl(bucket, a.getObjectKey());
    }

    public String buildPublicUrl(String bucket, String objectKey) {
        String bucketPart = urlEncode(bucket);
        String keyPart = encodePathPreservingSlashes(objectKey);
        // http://host/minio/{bucket}/{objectKey}
        return String.format("%s/%s/%s", trimRight(publicBaseUrl), bucketPart, keyPart);
    }

    private String trimRight(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String encodePathPreservingSlashes(String path) {
        if (path == null || path.isEmpty()) return "";
        return Arrays.stream(path.split("/"))
                .map(seg -> URLEncoder.encode(seg, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
    }

    // ---- UPLOAD HELPERS (как были) ----

    public ObjectWriteResponse uploadPng(byte[] bytes, String objectKey) throws Exception {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(objectKey)
                            .stream(in, bytes.length, -1)
                            .contentType("image/png")
                            .build());
        }
    }

    public ObjectWriteResponse upload(byte[] bytes, String objectKey, String contentType) throws Exception {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(objectKey)
                            .stream(in, bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        }
    }

    public byte[] generateSquarePng(int size, boolean black) throws Exception {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(black ? Color.BLACK : Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
