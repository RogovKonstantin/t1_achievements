package com.t1.achievements.service;

import com.t1.achievements.dto.SectionShortDto;
import com.t1.achievements.entity.Section;
import com.t1.achievements.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final SectionRepository sectionRepo;

    @Transactional(readOnly = true)
    public List<SectionShortDto> listCategories() {
        return sectionRepo.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(s -> new SectionShortDto(s.getId(), s.getName(), s.getDescription()))
                .toList();
    }
}
