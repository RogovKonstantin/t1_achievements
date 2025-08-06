package com.t1.achievements.util;

import com.t1.achievements.entity.*;
import com.t1.achievements.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementStageRepository achievementStageRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserAchievementStageRepository userAchievementStageRepository;
    private final CourseRepository courseRepository;

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            User admin = userRepository.save(User.builder()
                    .fullName("Администратор Ачивок")
                    .position("Системный администратор")
                    .department("IT")
                    .grade(5)
                    .hireDate(Date.valueOf(LocalDate.of(2020, 1, 1)))
                    .username("admin")
                    .password("admin123")
                    .role("ADMIN")
                    .build());

            User user1 = userRepository.save(User.builder()
                    .fullName("Иван Иванов")
                    .position("Разработчик")
                    .department("Разработка")
                    .grade(3)
                    .hireDate(Date.valueOf(LocalDate.of(2018, 3, 20)))
                    .username("ivan")
                    .password("ivan123")
                    .role("USER")
                    .build());

            User user2 = userRepository.save(User.builder()
                    .fullName("Ольга Петрова")
                    .position("Тестировщик")
                    .department("QA")
                    .grade(2)
                    .hireDate(Date.valueOf(LocalDate.of(2021, 7, 15)))
                    .username("olga")
                    .password("olga123")
                    .role("USER")
                    .build());

            User user3 = userRepository.save(User.builder()
                    .fullName("Алексей Смирнов")
                    .position("Аналитик")
                    .department("Аналитика")
                    .grade(4)
                    .hireDate(Date.valueOf(LocalDate.of(2019, 11, 5)))
                    .username("alex")
                    .password("alex123")
                    .role("USER")
                    .build());

            Achievement achv1 = achievementRepository.save(Achievement.builder()
                    .name("5 лет в компании")
                    .description("За выслугу лет")
                    .criteria("5 лет с даты трудоустройства")
                    .build());

            Achievement achv2 = achievementRepository.save(Achievement.builder()
                    .name("Прошел 3 курса")
                    .description("За прохождение трёх учебных курсов")
                    .criteria("Пройти не менее 3 курсов в системе обучения")
                    .build());

            Achievement achv3 = achievementRepository.save(Achievement.builder()
                    .name("Амбассадор проекта")
                    .description("За активное участие в презентациях")
                    .criteria("3 публичных выступления или презентации проекта")
                    .build());

            Achievement achv4 = achievementRepository.save(Achievement.builder()
                    .name("Участие в хакатоне")
                    .description("За участие в командных хакатонах")
                    .criteria("Минимум 1 участие в хакатоне")
                    .build());

            Achievement achv5 = achievementRepository.save(Achievement.builder()
                    .name("Менторство")
                    .description("За наставничество новых сотрудников")
                    .criteria("Ментор минимум одного новичка")
                    .build());

            Achievement achv6 = achievementRepository.save(Achievement.builder()
                    .name("100% вовлечённости")
                    .description("За активность на внутренних мероприятиях")
                    .criteria("Участие во всех мероприятиях месяца")
                    .build());

            achievementStageRepository.saveAll(List.of(
                    AchievementStage.builder().achievement(achv1).stageName("Более 3 лет в компании").required(true).build(),
                    AchievementStage.builder().achievement(achv1).stageName("Более 5 лет в компании").required(true).build(),

                    AchievementStage.builder().achievement(achv2).stageName("1 курс пройден").required(true).build(),
                    AchievementStage.builder().achievement(achv2).stageName("2 курса пройдены").required(true).build(),
                    AchievementStage.builder().achievement(achv2).stageName("3 курса пройдены").required(true).build(),

                    AchievementStage.builder().achievement(achv3).stageName("Первое выступление").required(true).build(),
                    AchievementStage.builder().achievement(achv3).stageName("Выступлений: 2").required(true).build(),
                    AchievementStage.builder().achievement(achv3).stageName("Выступлений: 3").required(true).build(),

                    AchievementStage.builder().achievement(achv4).stageName("Первый хакатон").required(true).build(),
                    AchievementStage.builder().achievement(achv5).stageName("Первый менторинг").required(true).build()
            ));

            userAchievementRepository.save(UserAchievement.builder()
                    .user(user1)
                    .achievement(achv1)
                    .achievedAt(Date.valueOf(LocalDate.of(2023, 1, 1)))
                    .build());

            userAchievementRepository.save(UserAchievement.builder()
                    .user(user2)
                    .achievement(achv2)
                    .achievedAt(Date.valueOf(LocalDate.of(2024, 4, 1)))
                    .build());

            userAchievementRepository.save(UserAchievement.builder()
                    .user(user3)
                    .achievement(achv5)
                    .achievedAt(Date.valueOf(LocalDate.of(2024, 3, 1)))
                    .build());

            courseRepository.saveAll(List.of(
                    Course.builder().user(user1).courseName("Spring Boot Advanced").completionDate(Date.valueOf(LocalDate.of(2023, 4, 1))).build(),
                    Course.builder().user(user1).courseName("PostgreSQL Basics").completionDate(Date.valueOf(LocalDate.of(2023, 6, 15))).build(),
                    Course.builder().user(user1).courseName("Microservices Architecture").completionDate(Date.valueOf(LocalDate.of(2023, 9, 10))).build(),

                    Course.builder().user(user2).courseName("REST API Design").completionDate(Date.valueOf(LocalDate.of(2024, 2, 10))).build(),
                    Course.builder().user(user2).courseName("Docker Essentials").completionDate(Date.valueOf(LocalDate.of(2024, 3, 1))).build(),

                    Course.builder().user(user3).courseName("Agile Foundations").completionDate(Date.valueOf(LocalDate.of(2022, 12, 12))).build(),
                    Course.builder().user(user3).courseName("Mentorship 101").completionDate(Date.valueOf(LocalDate.of(2023, 2, 20))).build()
            ));
        }
    }
}
