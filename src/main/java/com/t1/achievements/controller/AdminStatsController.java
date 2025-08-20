package com.t1.achievements.controller;

import com.t1.achievements.controller.api.AdminStatsApi;
import com.t1.achievements.dto.admin.AdminStatsDto;
import com.t1.achievements.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@Validated
@RequiredArgsConstructor
public class AdminStatsController implements AdminStatsApi {

    private final AdminStatsService service;

    @Override
    public AdminStatsDto getDashboardStats() {
        return service.getDashboardStats();
    }
}
