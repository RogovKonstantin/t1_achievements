package com.t1.achievements.service;

import com.t1.achievements.dto.AchievementDto;
import com.t1.achievements.entity.Achievement;
import com.t1.achievements.entity.UserAchievement;
import com.t1.achievements.repository.AchievementRepository;
import com.t1.achievements.repository.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ModelMapper modelMapper;

    public List<AchievementDto> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AchievementDto> getUserAchievements(UUID userId) {
        return userAchievementRepository.findByUserId(userId).stream()
                .map(UserAchievement::getAchievement)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AchievementDto convertToDto(Achievement achievement) {
        return modelMapper.map(achievement, AchievementDto.class);
    }
}
