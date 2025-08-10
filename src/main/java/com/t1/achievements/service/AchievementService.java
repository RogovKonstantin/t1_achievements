// com.t1.achievements.service.AchievementService.java
package com.t1.achievements.service;

import com.t1.achievements.dto.AchievementCardDto;
import com.t1.achievements.entity.*;
import com.t1.achievements.exception.BadRequestException;
import com.t1.achievements.exception.NotFoundException;
import com.t1.achievements.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepo;
    private final UserAchievementRepository userAchRepo;
    private final AchievementStageRepository stageRepo;
    private final UserRepository userRepo;

    @Value("${minio.endpoint}") private String minioEndpoint;

    /** Вернуть все ачивки. Пустой список = 200 OK. */
    public List<AchievementCardDto> getAllAchievements() {
        try {
            List<Achievement> list = achievementRepo.findAllWithDeps();
            Map<UUID, List<AchievementStage>> stagesByAch = loadStages(list);

            return list.stream()
                    .map(a -> toCard(a, stagesByAch.getOrDefault(a.getId(), List.of()), null))
                    .toList();
        } catch (DataAccessException dae) {
            throw new RuntimeException("Ошибка доступа к данным при получении списка ачивок", dae);
        }
    }

    /** Вернуть ачивки конкретного пользователя.
     *  400 — если userId null;
     *  404 — если пользователя нет;
     *  200 — даже если список пустой.
     */
    public List<AchievementCardDto> getUserAchievements(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("Параметр userId обязателен");
        }
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }

        try {
            List<UserAchievement> uaList = userAchRepo.findByUserIdWithDeps(userId);
            if (uaList.isEmpty()) {
                return List.of(); // не ошибка
            }

            // Подтянем стадии одним махом
            List<Achievement> achs = uaList.stream().map(UserAchievement::getAchievement).toList();
            Map<UUID, List<AchievementStage>> stagesByAch = loadStages(achs);

            return uaList.stream()
                    .map(ua -> {
                        Achievement a = ua.getAchievement();
                        return toCard(a, stagesByAch.getOrDefault(a.getId(), List.of()), ua);
                    })
                    .toList();
        } catch (DataAccessException dae) {
            throw new RuntimeException("Ошибка доступа к данным при получении ачивок пользователя", dae);
        }
    }

    // -------- helpers --------

    private Map<UUID, List<AchievementStage>> loadStages(List<Achievement> list) {
        if (list == null || list.isEmpty()) return Map.of();
        List<UUID> ids = list.stream().map(Achievement::getId).toList();
        return stageRepo.findByAchievementIdIn(ids).stream()
                .collect(Collectors.groupingBy(st -> st.getAchievement().getId()));
    }

    private AchievementCardDto toCard(Achievement a,
                                      List<AchievementStage> stages,
                                      UserAchievement uaOrNull) {

        AchievementCardDto.CreatedByDto createdBy = null;
        if (a.getCreatedBy() != null) {
            createdBy = AchievementCardDto.CreatedByDto.builder()
                    .id(s(a.getCreatedBy().getId()))
                    .fullName(a.getCreatedBy().getFullName())
                    .build();
        }

        List<AchievementCardDto.SectionDto> sectionDtos = a.getSections().stream()
                .sorted(Comparator.comparing(Section::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(s -> AchievementCardDto.SectionDto.builder()
                        .id(s(s.getId()))
                        .code(s.getCode())
                        .title(s.getName())
                        .build())
                .toList();

        List<AchievementCardDto.TagDto> tagDtos = a.getTags().stream()
                .map(t -> AchievementCardDto.TagDto.builder()
                        .id(s(t.getId()))
                        .code(t.getCode())
                        .name(t.getName())
                        .build())
                .toList();

        String iconUrl   = assetUrl(a.getIcon());
        String bannerUrl = assetUrl(a.getBanner());

        AchievementCardDto.AchievementCardDtoBuilder b = AchievementCardDto.builder()
                .id(s(a.getId()))
                .code(a.getCode())
                .title(a.getTitle())
                .shortDescription(a.getShortDescription())
                .points(a.getPoints())
                .repeatable(a.getRepeatable())
                .visibility(a.getVisibility().name())
                .active(a.getActive())
                .iconUrl(iconUrl)
                .bannerUrl(bannerUrl)
                .sections(sectionDtos)
                .tags(tagDtos)
                .createdBy(createdBy);

        if (uaOrNull != null) {
            if (uaOrNull.getAwardedAt() != null) {
                b.awardedAt(DateTimeFormatter.ISO_INSTANT.format(uaOrNull.getAwardedAt()));
            }
            if (uaOrNull.getMethod() != null) {
                b.awardMethod(uaOrNull.getMethod().name()); // AUTO | MANUAL
            }
        }

        return b.build();
    }

    private String assetUrl(Asset asset) {
        if (asset == null) return null;
        return String.format("%s/%s/%s", minioEndpoint, asset.getBucket(), asset.getObjectKey());
    }

    private static String s(Object o) { return o == null ? null : o.toString(); }
}
