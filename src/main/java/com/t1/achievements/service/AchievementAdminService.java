package com.t1.achievements.service;

import com.t1.achievements.dto.AchievementCategoriesDto;
import com.t1.achievements.dto.SectionShortDto;
import com.t1.achievements.dto.UserListItemDto;
import com.t1.achievements.entity.Achievement;
import com.t1.achievements.entity.Asset;
import com.t1.achievements.entity.Section;
import com.t1.achievements.entity.User;
import com.t1.achievements.repository.AchievementRepository;
import com.t1.achievements.repository.SectionRepository;
import com.t1.achievements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AchievementAdminService {

    private final AchievementRepository achievementRepo;
    private final SectionRepository sectionRepo;
    private final UserRepository userRepo;
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
}
