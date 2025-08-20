package com.t1.achievements.service;

import com.t1.achievements.RR.CriterionInput;
import com.t1.achievements.dto.*;
import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.t1.achievements.RR.CreateSectionRequest;
import com.t1.achievements.RR.CreateAchievementRequest;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AchievementAdminService {

    private final AchievementRepository achievementRepo;
    private final SectionRepository sectionRepo;
    private final UserRepository userRepo;

    private final ActivityTypeRepository activityTypeRepo;
    private final AchievementCriterionRepository criterionRepo;
    private final AssetRepository assetRepo;
    private final AssetStorageService storage;


    private final AssetStorageService assets;
    @Value("${minio.bucket}")
    private String bucket;
    private String assetUrl(Asset a) {
        return assets.publicUrl(a);
    }


    @Transactional(readOnly = true)
    public Page<UserListItemDto> getHolders(UUID achievementId, Pageable pageable) {
        Page<User> page = userRepo.findHoldersByAchievementId(achievementId, pageable);
        return page.map(u -> new UserListItemDto(
                u.getId(),
                u.getFullName(),
                u.getDepartment(),
                u.getPosition(),
                assetUrl(u.getAvatar())
        ));
    }

    @Transactional
    public AchievementCategoriesDto updateCategories(UUID achievementId, Set<UUID> sectionIds) {
        Achievement a = achievementRepo.findById(achievementId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ачивка не найдена"));

        List<Section> sections = sectionIds == null || sectionIds.isEmpty()
                ? List.of()
                : sectionRepo.findAllById(sectionIds);

        if (sectionIds != null && sections.size() != sectionIds.size()) {
            throw new ResponseStatusException(NOT_FOUND, "Некоторые категории не найдены");
        }

        a.setSections(new HashSet<>(sections));
        achievementRepo.save(a);

        List<SectionShortDto> dtos = sections.stream()
                .map(s -> new SectionShortDto(s.getId(), s.getName(), s.getDescription()))
                .collect(Collectors.toList());

        return new AchievementCategoriesDto(a.getId(), dtos);
    }

    @Transactional
    public SectionDto createSection(CreateSectionRequest req) {
        Section s = Section.builder()
                .name(req.name())
                .description(req.description())
                .code(req.name().toUpperCase(Locale.ROOT))
                .sortOrder(100)
                .active(true)
                .build();

        s = sectionRepo.save(s);

        return SectionDto.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .description(s.getDescription())
                .sortOrder(s.getSortOrder())
                .active(s.getActive())
                .build();
    }

    private String slugify(String name) {
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9а-яё]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.length() > 64 ? slug.substring(0, 64) : slug;
    }

    @Transactional(readOnly = true)
    public List<CriterionTypeDto> listCriterionTypesForForm() {
        List<CriterionTypeDto> list = new ArrayList<>();

        if (activityTypeRepo.existsByCodeIgnoreCase("TENURE_DAYS")) {
            list.add(new CriterionTypeDto(
                    "TENURE_YEARS",
                    "Стаж (лет)",
                    "Минимальный стаж работы в компании (в годах).",
                    "number",
                    "лет"
            ));
        }

        activityTypeRepo.findAllActive().forEach(t -> {
            if (!"TENURE_DAYS".equalsIgnoreCase(t.getCode())) {
                list.add(new CriterionTypeDto(
                        t.getCode(),
                        t.getName(),
                        t.getDescription() == null ? "" : t.getDescription(),
                        "number",
                        null
                ));
            }
        });

        return list;
    }

    @Transactional
    public AchievementDto createAchievement(CreateAchievementRequest req,
                                            MultipartFile iconFile,
                                            MultipartFile animationFile) {
        if (iconFile == null || iconFile.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Иконка обязательна");
        }

        Section section = sectionRepo.findById(req.sectionId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Раздел не найден"));

        // === сохраняем по тем же путям, что в сидере ===
        String iconName = sanitize(iconFile.getOriginalFilename());
        Asset icon = uploadAndSave(iconFile, "icons/" + iconName); // image/png|jpg и пр.

        Asset animation = null;
        if (animationFile != null && !animationFile.isEmpty()) {
            String animName = sanitize(animationFile.getOriginalFilename());
            animation = uploadAndSave(animationFile, "animations/" + animName); // ожидаем image/gif
        }

        Achievement a = Achievement.builder()
                .title(req.title())
                .shortDescription(autoShort(req.descriptionMd()))
                .descriptionMd(req.descriptionMd())
                .visibility(Achievement.Visibility.PUBLIC)
                .active(true)
                .icon(icon)
                .animation(animation)   // <- добавили
                .points(req.points())
                .repeatable(false)
                .build();

        // обязательно, иначе NPE при add
        if (a.getSections() == null) a.setSections(new HashSet<>());
        a.getSections().add(section);

        a = achievementRepo.save(a);

        if (req.criteria() != null) {
            int i = 0;
            for (CriterionInput in : req.criteria()) {
                MappedCriterion mc = mapUiCriterionToActivityType(in);
                ActivityType type = activityTypeRepo.findByCodeIgnoreCase(mc.code())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ActivityType не найден: " + mc.code()));
                criterionRepo.save(AchievementCriterion.builder()
                        .achievement(a)
                        .activityType(type)
                        .requiredCount(mc.requiredCount())
                        .withinDays(in.withinDays())
                        .descriptionOverride(in.descriptionOverride())
                        .sortOrder(in.sortOrder() != null ? in.sortOrder() : (++i) * 10)
                        .build());
            }
        }

        return new AchievementDto(
                a.getId(), a.getTitle(), a.getShortDescription(), a.getDescriptionMd(),
                section.getId(), icon.getId(), a.getVisibility().name(), a.getActive()
        );
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        String name = filename.replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1); // только basename
        // не трогаем регистр/расширение, уберём только совсем опасные символы
        return name.replaceAll("[\\r\\n\\t]", "_");
    }

    private Asset uploadAndSave(MultipartFile file, String objectKey) {
        try {
            byte[] bytes = file.getBytes();
            String ct = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            // используем те же методы, что и сидер
            ObjectWriteResponse resp = "image/png".equalsIgnoreCase(ct)
                    ? storage.uploadPng(bytes, objectKey)
                    : storage.upload(bytes, objectKey, ct);

            return assetRepo.save(Asset.builder()
                    .bucket(bucket)
                    .objectKey(objectKey)
                    .versionId(Optional.ofNullable(resp.versionId()).orElse(""))
                    .contentType(ct)
                    .sizeBytes((long) bytes.length)
                    .etag(resp.etag())
                    .build());
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST, "Не удалось загрузить файл: " + objectKey, e);
        }
    }

    private MappedCriterion mapUiCriterionToActivityType(CriterionInput in) {
        String ui = in.typeCode().toUpperCase(Locale.ROOT);
        int val = in.value();
        if ("TENURE_YEARS".equals(ui)) return new MappedCriterion("TENURE_DAYS", Math.toIntExact((long) val * 365));
        return new MappedCriterion(ui, val);
    }
    private record MappedCriterion(String code, int requiredCount) {}


    private String autoShort(String md) {
        if (md == null) return null;
        // грубо уберём Markdown и обрежем
        String s = md.replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1") // ссылки
                .replaceAll("[*_`#>]+", "")               // базовые маркдауны
                .replaceAll("\\s+", " ")
                .trim();
        int max = 180;
        return s.length() > max ? s.substring(0, max).trim() + "…" : s;
    }

}
