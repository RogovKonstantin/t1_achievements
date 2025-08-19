package com.t1.achievements.service;

import com.t1.achievements.RR.CriterionInput;
import com.t1.achievements.dto.*;
import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import lombok.RequiredArgsConstructor;
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

    private final AssetStorageService assets;

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
    public AchievementDto createAchievement(CreateAchievementRequest req, MultipartFile iconFile) {
        Section section = sectionRepo.findById(req.sectionId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Раздел не найден"));

        Asset icon = null;
        if (iconFile != null && !iconFile.isEmpty()) {
            icon = assets.store(iconFile, "achievements/icons/");
        } else if (req.iconAssetId() != null) {
            icon = assetRepo.findById(req.iconAssetId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Иконка (Asset) не найдена"));
        }

        Achievement.Visibility vis = req.visibility() == null
                ? Achievement.Visibility.PUBLIC
                : Achievement.Visibility.valueOf(req.visibility().toUpperCase(Locale.ROOT));

        Achievement a = Achievement.builder()
                .title(req.title())
                .shortDescription(req.shortDescription())
                .descriptionMd(req.descriptionMd())
                .active(req.active() == null || req.active())
                .icon(icon)
                .build();
        a.setVisibility(vis);
        a.getSections().add(section);

        a = achievementRepo.save(a);

        if (req.criteria() != null) {
            int i = 0;
            for (CriterionInput in : req.criteria()) {
                MappedCriterion mapped = mapUiCriterionToActivityType(in);
                ActivityType type = activityTypeRepo.findByCodeIgnoreCase(mapped.code())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ActivityType не найден: " + mapped.code()));

                AchievementCriterion c = AchievementCriterion.builder()
                        .achievement(a)
                        .activityType(type)
                        .requiredCount(mapped.requiredCount())
                        .withinDays(in.withinDays())
                        .descriptionOverride(in.descriptionOverride())
                        .sortOrder(in.sortOrder() != null ? in.sortOrder() : (++i) * 10)
                        .build();

                criterionRepo.save(c);
            }
        }

        return new AchievementDto(
                a.getId(),
                a.getTitle(),
                a.getShortDescription(),
                a.getDescriptionMd(),
                section.getId(),
                icon != null ? icon.getId() : null,
                a.getVisibility().name(),
                a.getActive()
        );
    }

    private MappedCriterion mapUiCriterionToActivityType(CriterionInput in) {
        String ui = in.typeCode().toUpperCase(Locale.ROOT);
        int val = in.value();
        if ("TENURE_YEARS".equals(ui)) {
            return new MappedCriterion("TENURE_DAYS", Math.toIntExact((long) val * 365));
        }
        return new MappedCriterion(ui, val);
    }

    private record MappedCriterion(String code, int requiredCount) {}
}
