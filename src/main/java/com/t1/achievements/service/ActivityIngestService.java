package com.t1.achievements.service;

import com.t1.achievements.entity.ActivityLog;
import com.t1.achievements.entity.ActivityType;
import com.t1.achievements.entity.User;
import com.t1.achievements.repository.ActivityLogRepository;
import com.t1.achievements.repository.ActivityTypeRepository;
import com.t1.achievements.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityIngestService {

    private final ActivityTypeRepository activityTypeRepo;
    private final ActivityLogRepository activityLogRepo;
    private final UserRepository userRepo;
    private final AchievementProgressService progressService;

    @Transactional
    public void registerTestPassed(
            String testCode,
            UUID userId
    ) {
        ActivityType type = activityTypeRepo.findByCode(testCode)
                .orElseGet(() -> activityTypeRepo.save(
                        ActivityType.builder()
                                .code(testCode)
                                .name("Test passed: " + testCode)
                                .description("Auto-created activity type for test completion")
                                .sourceSystem("tests-frontend")
                                .active(true)
                                .build()
                ));

        User user = userRepo.findById(userId).orElseThrow();

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .activityType(type)
                .occurredAt(Instant.now())
                .sourceSystem("tests-frontend")
                .sourceEventId("")
                .build();
        activityLogRepo.save(log);

        progressService.recalculateForUserByActivityType(user, type);
    }
}
