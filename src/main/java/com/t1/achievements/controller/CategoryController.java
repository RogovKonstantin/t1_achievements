// com.t1.achievements.controller.CategoryController
package com.t1.achievements.controller;

import com.t1.achievements.controller.api.CategoryApi;
import com.t1.achievements.dto.SectionShortDto;
import com.t1.achievements.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CategoryController implements CategoryApi {

    private final CategoryService service;

    @Override
    public List<SectionShortDto> listCategories() {
        return service.listCategories();
    }
}
