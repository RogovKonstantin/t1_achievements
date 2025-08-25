package com.t1.achievements.service;

import com.t1.achievements.entity.Asset;
import com.t1.achievements.repository.AssetRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetStorageService {

    private final MinioClient minio;
    private final AssetRepository assetRepo;

    @Value("${minio.bucket}")
    private String defaultBucket;

    @Value("${minio.publicBaseUrl}")
    private String publicBaseUrl;

    public Asset store(MultipartFile file, String prefix) {
        try {
            String ext = guessExt(file.getOriginalFilename());
            String key = (prefix == null ? "" : prefix) + UUID.randomUUID() + (ext == null ? "" : ext);
            String contentType = file.getContentType();

            var resp = upload(file.getBytes(), key, contentType == null ? "application/octet-stream" : contentType);

            Asset a = Asset.builder()
                    .bucket(defaultBucket)
                    .objectKey(key)
                    .versionId(resp.versionId() == null ? "" : resp.versionId())
                    .etag(resp.etag())
                    .contentType(contentType)
                    .sizeBytes(file.getSize())
                    .build();

            return assetRepo.save(a);
        } catch (Exception e) {
            throw new RuntimeException("Asset upload failed", e);
        }
    }

    private String guessExt(String filename) {
        if (filename == null) return null;
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : null;
    }

    @PostConstruct
    public void ensureBucket() throws Exception {
        boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(defaultBucket).build());
        if (!exists) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(defaultBucket).build());
        }
    }


    public String publicUrl(Asset a) {
        if (a == null) return null;
        String bucket = a.getBucket() != null ? a.getBucket() : defaultBucket;
        return buildPublicUrl(bucket, a.getObjectKey());
    }

    public String buildPublicUrl(String bucket, String objectKey) {
        String bucketPart = urlEncode(bucket);
        String keyPart = encodePathPreservingSlashes(objectKey);
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
