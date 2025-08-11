package com.t1.achievements.controller;

import com.t1.achievements.entity.Asset;
import com.t1.achievements.repository.AssetRepository;
import com.t1.achievements.service.AssetStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AssetController {
    private final AssetRepository assetRepo;
    private final AssetStorageService storage;

    @GetMapping("/assets/{id}")
    public ResponseEntity<Void> getAssetRedirect(@PathVariable String id) throws Exception {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        Asset a = assetRepo.findById(uuid)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        String url = storage.presignedGet(a.getObjectKey(), java.time.Duration.ofMinutes(30));
        return ResponseEntity.status(307)
                .header("Location", url)
                .header("Cache-Control", "private, max-age=60")
                .build();
    }}
