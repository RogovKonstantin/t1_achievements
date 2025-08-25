package com.t1.achievements.controller;

import com.t1.achievements.RR.CreateAchievementRequest;
import com.t1.achievements.RR.CreateSectionRequest;
import com.t1.achievements.RR.UpdateAchievementRequest;
import com.t1.achievements.RR.UpdateSectionRequest;
import com.t1.achievements.controller.api.AchievementAdminApi;
import com.t1.achievements.dto.*;
import com.t1.achievements.dto.admin.AchievementAdminFullDto;
import com.t1.achievements.dto.admin.UserAchievementGrantDto;
import com.t1.achievements.exception.StatusResponse;
import com.t1.achievements.service.AchievementAdminService;
import com.t1.achievements.service.AchievementProgressService;
import com.t1.achievements.service.AdminAchievementQueryService;
import com.t1.achievements.service.AdminAchievementSeedService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    private final AdminAchievementSeedService seedService;
    private final AchievementProgressService progressService;

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

    @Override
    public AchievementDto updateAchievement(UUID achievementId, @Valid UpdateAchievementRequest request,
                                            MultipartFile icon, MultipartFile animation) {
        return service.updateAchievement(achievementId, request, icon, animation);
    }

    @Override
    public SectionDto updateSection(UUID sectionId, UpdateSectionRequest body) {
        return service.updateSection(sectionId, body);
    }
    @Override
    public UserAchievementGrantDto grantAchievementToUser(UUID userId, UUID achievementId) {
        return service.grantAchievementToUser(userId, achievementId);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAchievementFromUser(UUID userId, UUID achievementId) {
        service.revokeAchievementFromUser(userId, achievementId);
    }

    @Operation(summary = "Инициализировать прогресс 0/N для всех пользователей (массово)")
    @PostMapping("/{achievementId}/seed-progress")
    public ResponseEntity<StatusResponse> seedProgress(
            @PathVariable @NotNull UUID achievementId,
            @RequestParam(defaultValue = "true") boolean reset
    ) {
        var res = seedService.seedProgressZero(achievementId, reset);
        return ResponseEntity.ok(new StatusResponse(
                "ok",
                "processed=%d, created=%d, updated=%d, totalSteps=%d"
                        .formatted(res.processedUsers(), res.created(), res.updated(), res.totalSteps())
        ));
    }

    @Operation(summary = "Ручной пересчёт прогресса пользователя по ачивке")
    @PostMapping("/{achievementId}/recalc/{userId}")
    public ResponseEntity<StatusResponse> recalcForUser(
            @PathVariable UUID achievementId,
            @PathVariable UUID userId
    ) {
        progressService.recalcForUserAndAchievement(userId, achievementId);
        return ResponseEntity.ok(new StatusResponse("ok", "recalculated"));
    }
}


