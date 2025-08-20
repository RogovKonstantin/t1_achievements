package com.t1.achievements.controller;

import com.t1.achievements.RR.CreateAchievementRequest;
import com.t1.achievements.RR.CreateSectionRequest;
import com.t1.achievements.controller.api.AchievementAdminApi;
import com.t1.achievements.dto.*;
import com.t1.achievements.dto.admin.AchievementAdminFullDto;
import com.t1.achievements.service.AchievementAdminService;
import com.t1.achievements.service.AdminAchievementQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@Validated
public class AchievementAdminController implements AchievementAdminApi {

    private final AchievementAdminService service;
    private final AdminAchievementQueryService queryService;

    @Override
    public PageResponse<UserListItemDto> getHolders(UUID achievementId, @ParameterObject Pageable pageable) {
        return PageResponse.from(service.getHolders(achievementId, pageable));
    }

    @Override
    public AchievementCategoriesDto updateCategories(UUID achievementId, UpdateCategoriesRequest body) {
        return service.updateCategories(achievementId, body == null ? null : body.categoryIds());
    }

    @Override
    public SectionDto createSection(CreateSectionRequest body) {
        return service.createSection(body);

    }

    @Override
    public List<CriterionTypeDto> listCriterionTypes() {
        return service.listCriterionTypesForForm();
    }

    @Override
    public AchievementDto createAchievement(CreateAchievementRequest request, MultipartFile icon, MultipartFile animation) {
        return service.createAchievement(request, icon, animation);
    }
    @Override
    public List<AchievementAdminFullDto> listAllAchievementsFull() {
        return queryService.listAllAchievementsFull();
    }

}
