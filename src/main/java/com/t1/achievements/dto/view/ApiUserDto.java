package com.t1.achievements.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiUserDto", description = "Краткая инфа о пользователе для экрана ачивок")
public record ApiUserDto(
        @Schema(example = "Иванов Иван")
        String fullName,
        @Schema(example = "Отдел разработки")
        String department,
        @Schema(example = "Backend-разработчик")
        String position,
        @Schema(example = "https://.../avatar.png")
        String avatarUrl,
        @Schema(example = "+7 999 123 45-67")
        String phone,
        @Schema(example = "12--03--2021", description = "Дата трудоустройства в формате dd--MM--yyyy")
        String hireDate,
        @Schema(example = "ivanov@t1.ru")
        String email
) {}
