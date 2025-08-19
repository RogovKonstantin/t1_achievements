package com.t1.achievements.controller;

import com.t1.achievements.controller.api.UserApi;
import com.t1.achievements.dto.PageResponse;
import com.t1.achievements.dto.UserListItemDto;
import com.t1.achievements.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UserController implements UserApi {

    private final UserQueryService service;

    @Override
    public PageResponse<UserListItemDto> listUsers(@ParameterObject Pageable pageable) {
        return service.listUsers(pageable);
    }
}
