// com.t1.achievements.controller.AchievementAdminController
package com.t1.achievements.controller;

import com.t1.achievements.controller.api.AchievementAdminApi;
import com.t1.achievements.dto.*;
import com.t1.achievements.service.AchievementAdminService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@Validated
public class AchievementAdminController implements AchievementAdminApi {

    private final AchievementAdminService service;

    @Override
    public PageResponse<UserListItemDto> getHolders(UUID achievementId, @ParameterObject Pageable pageable) {
        return PageResponse.from(service.getHolders(achievementId, pageable));
    }

    @Override
    public AchievementCategoriesDto updateCategories(UUID achievementId, UpdateCategoriesRequest body) {
        return service.updateCategories(achievementId, body == null ? null : body.categoryIds());
    }
}
