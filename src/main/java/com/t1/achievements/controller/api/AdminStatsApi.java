package com.t1.achievements.controller.api;

import com.t1.achievements.dto.admin.AdminStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "admin-stats", description = "Админская статистика по ачивкам")
@RequestMapping("/admin/stats")
public interface AdminStatsApi {

    @Operation(summary = "Сводная статистика для дашборда админа")
    @GetMapping
    AdminStatsDto getDashboardStats();
}
