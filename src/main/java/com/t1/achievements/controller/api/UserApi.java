// com.t1.achievements.controller.api.UserApi
package com.t1.achievements.controller.api;

import com.t1.achievements.dto.PageResponse;
import com.t1.achievements.dto.UserListItemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "users", description = "API пользователей")
@RequestMapping("/users")
public interface UserApi {

    @Operation(summary = "Список пользователей (пагинация)")
    @GetMapping
    PageResponse<UserListItemDto> listUsers(@ParameterObject Pageable pageable);
}
