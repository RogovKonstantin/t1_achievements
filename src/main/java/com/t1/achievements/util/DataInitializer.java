package com.t1.achievements.util;

import com.t1.achievements.entity.*;
import com.t1.achievements.entity.Achievement.Visibility;
import com.t1.achievements.repository.*;
import com.t1.achievements.service.AssetStorageService;
import io.minio.ObjectWriteResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final SectionRepository sectionRepo;
    private final AssetRepository assetRepo;
    private final AchievementRepository achievementRepo;
    private final AchievementCriterionRepository criterionRepo;
    private final AchievementStageRepository stageRepo;
    private final ActivityTypeRepository activityTypeRepo;
    private final ActivityLogRepository activityLogRepo;
    private final UserAchievementProgressRepository progressRepo;
    private final UserAchievementRepository userAchRepo;
    private final AssetStorageService storage;
    private final PasswordEncoder passwordEncoder;


    private record Crit(String activityCode, int required, Integer withinDays) {
    }

    private record AchSpec(String code, String title, String shortDesc, boolean manual,
                           List<String> sectionCodes, List<Crit> criteria) {
    }

    private static final ZoneId Z = ZoneId.systemDefault();
    private final Random rnd = new Random(42);
    @Value("${minio.bucket}")
    private String bucket;

    private Role upsertRole(String code, String name) {
        return roleRepo.findByCode(code)
                .orElseGet(() -> roleRepo.save(Role.builder().code(code).name(name).build()));
    }

    @PostConstruct
    @Transactional
    public void init() throws Exception {
        if (userRepo.count() > 0 || achievementRepo.count() > 0) return;

        Role admin = upsertRole("ADMIN", "Administrator");
        Role userRole = upsertRole("USER", "User");

        List<User> admins = new ArrayList<>();
        String hashed = passwordEncoder.encode("password");

        for (int i = 1; i <= 3; i++) {
            admins.add(userRepo.save(
                    User.builder()
                            .username("admin" + i)
                            .password(hashed) // ← уже зашифровано
                            .fullName("Admin " + i)
                            .email("admin" + i + "@t1.local")
                            .position("Administrator")
                            .department("IT")
                            .grade(5)
                            .active(true)
                            .roles(new HashSet<>(Set.of(admin, userRole)))
                            .hireDate(LocalDate.now().minusYears(3 + i))
                            .avatar(createAvatar("admin" + i))
                            .build()
            ));
        }

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            users.add(userRepo.save(
                    User.builder()
                            .username("user%03d".formatted(i))
                            .password(hashed) // ← уже зашифровано
                            .fullName("User %03d".formatted(i))
                            .email("user%03d@t1.local".formatted(i))
                            .position(sample(List.of("Developer", "Analyst", "QA", "DevOps", "PM", "Designer")))
                            .department(sample(List.of("R&D", "Delivery", "HR", "Marketing", "Finance", "Ops")))
                            .grade(1 + rnd.nextInt(5))
                            .active(true)
                            .roles(new HashSet<>(Set.of(userRole)))
                            .hireDate(LocalDate.now().minusDays(200 + rnd.nextInt(1200)))
                            .avatar(createAvatar("user" + i))
                            .build()
            ));
        }

        Map<String, Section> sections = saveSections(
                Map.of(
                        "LEARN", new Section(null, "LEARN", "Обучение", "Курсы, знания", 10, true, Instant.now()),
                        "CYBER", new Section(null, "CYBER", "Кибертурниры", "Киберспорт", 20, true, Instant.now()),
                        "COMMUNITY", new Section(null, "COMMUNITY", "Сообщество", "Ивенты, статьи, волонтёрство", 30, true, Instant.now()),
                        "DOCS", new Section(null, "DOCS", "Документы", "HR-Link, подписи", 40, true, Instant.now()),
                        "PROFILE", new Section(null, "PROFILE", "Профиль", "Заполнение профиля", 50, true, Instant.now()),
                        "ANNIV", new Section(null, "ANNIV", "Стаж", "Годы в команде", 60, true, Instant.now()),
                        "VAC", new Section(null, "VAC", "Отпуск", "Баланс отпуска", 70, true, Instant.now())
                )
        );


        Map<String, Asset> icons = new HashMap<>();
        Map<String, Asset> banners = new HashMap<>();
        Map<String, Asset> animations = new HashMap<>();
        byte[] gifBytes;
        {
            var res = new ClassPathResource("icons8-favicon.gif");
            try (var in = res.getInputStream()) {
                gifBytes = in.readAllBytes();
            }
        }
        for (String code : List.of(
                "ZNATOK", "CYBER_CHAMPION", "CYBER_MASTER", "MUZYKANT1", "PODPISANT",
                "PROFILE_100", "TEAM_YEAR_2", "TEAM_YEAR_3", "TEAM_YEAR_4",
                "VAC_BALANCE", "HOST", "CASE_MAKER", "BUGATHON", "EDU_ARCH",
                "RESEARCHER", "AUTHOR_T1", "IT_CAMP"
        )) {
            String iconKey = "icons/%s.png".formatted(code);
            byte[] iconBytes = storage.generateSquarePng(256, rnd.nextBoolean());
            String animKey = "animations/%s.gif".formatted(code);
            ObjectWriteResponse animResp = storage.upload(gifBytes, animKey, "image/gif");
            ObjectWriteResponse iconResp = storage.uploadPng(iconBytes, iconKey);

            Asset icon = assetRepo.save(Asset.builder()
                    .bucket(bucket)
                    .objectKey(iconKey)
                    .versionId(Optional.ofNullable(iconResp.versionId()).orElse(""))
                    .contentType("image/png")
                    .sizeBytes((long) iconBytes.length)
                    .etag(iconResp.etag())
                    .build());
            icons.put(code, icon);

            String bannerKey = "banners/%s.png".formatted(code);
            byte[] bannerBytes = storage.generateSquarePng(360, rnd.nextBoolean()); // простой заглушкой
            ObjectWriteResponse bannerResp = storage.uploadPng(bannerBytes, bannerKey);

            Asset banner = assetRepo.save(Asset.builder()
                    .bucket(bucket)
                    .objectKey(bannerKey)
                    .versionId(Optional.ofNullable(bannerResp.versionId()).orElse(""))
                    .contentType("image/png")
                    .sizeBytes((long) bannerBytes.length)
                    .etag(bannerResp.etag())
                    .build());
            banners.put(code, banner);
            Asset anim = assetRepo.save(Asset.builder()
                    .bucket(bucket)
                    .objectKey(animKey)
                    .versionId(Optional.ofNullable(animResp.versionId()).orElse(""))
                    .contentType("image/gif")
                    .sizeBytes((long) gifBytes.length)
                    .etag(animResp.etag())
                    .build());
            animations.put(code, anim);
        }

        Map<String, ActivityType> act = saveActivityTypes(
                List.of(
                        new ActivityType(null, "LMS_OBLIG_CLEAR", "Нет просроченных обязательных курсов", "Снято ограничение в LMS", "LMS", true, Instant.now()),
                        new ActivityType(null, "CYBER_TOURNAMENT_WIN", "Победа в кибертурнире", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "CYBER_TOURNAMENT_PRIZE", "Призовое место в кибертурнире", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "MUSIC_GROUP_PERF", "Участие в музыкальных выступлениях", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "HRLINK_MONTH_CLEAR", "Месяц без долга по подписям", null, "HR-LINK", true, Instant.now()),
                        new ActivityType(null, "PROFILE_100", "Профиль заполнен на 100%", null, "PORTAL", true, Instant.now()),
                        new ActivityType(null, "WORK_YEAR", "Засчитанный год стажа", null, "HR", true, Instant.now()),
                        new ActivityType(null, "VAC_BALANCE_OK", "Остаток отпуска ≤ 5 дней на конец периода", null, "HR", true, Instant.now()),
                        new ActivityType(null, "INTERNAL_EVENT_HOST", "Ведущий внутреннего мероприятия", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "HACK_CASE_AUTHOR", "Автор кейса для хакатона", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "BUGATHON_PARTICIPANT", "Участник багатона", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "EDU_ARCH_HELP", "Помощь в разработке обучающих программ", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "FOCUS_GROUP", "Участие в фокус-группе/исследовании", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "PRO_ARTICLE", "Публикация статьи в профильном издании", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null, "IT_CAMP_VOL", "Волонтёр ИТ-Лагеря", null, "MANUAL", true, Instant.now())
                )
        );

        List<AchSpec> specs = List.of(
                new AchSpec("ZNATOK", "Знаток", "Нет просроченных обязательных курсов", false,
                        List.of("LEARN"),
                        List.of(new Crit("LMS_OBLIG_CLEAR", 1, null))),
                new AchSpec("CYBER_CHAMPION", "Кибер чемпион", "Победа в кибертурнире", true,
                        List.of("CYBER"),
                        List.of(new Crit("CYBER_TOURNAMENT_WIN", 1, null))),
                new AchSpec("CYBER_MASTER", "Кибер мастер", "Призовое место в кибертурнире", true,
                        List.of("CYBER"),
                        List.of(new Crit("CYBER_TOURNAMENT_PRIZE", 1, null))),
                new AchSpec("MUZYKANT1", "МузыканТ1", "Участие в музыкальных группах и выступлениях", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("MUSIC_GROUP_PERF", 1, null))),
                new AchSpec("PODPISANT", "Подписант", "12 месяцев подряд без долга по подписям", false,
                        List.of("DOCS"),
                        List.of(new Crit("HRLINK_MONTH_CLEAR", 12, 365))),
                new AchSpec("PROFILE_100", "Идеальный профиль", "100% заполненный профиль", false,
                        List.of("PROFILE"),
                        List.of(new Crit("PROFILE_100", 1, null))),
                new AchSpec("TEAM_YEAR_2", "Год в команде 2", "2 полных года в компании", false,
                        List.of("ANNIV"),
                        List.of(new Crit("WORK_YEAR", 2, null))),
                new AchSpec("TEAM_YEAR_3", "Год в команде 3", "3 полных года в компании", false,
                        List.of("ANNIV"),
                        List.of(new Crit("WORK_YEAR", 3, null))),
                new AchSpec("TEAM_YEAR_4", "Год в команде 4", "4 полных года в компании", false,
                        List.of("ANNIV"),
                        List.of(new Crit("WORK_YEAR", 4, null))),
                new AchSpec("VAC_BALANCE", "В балансе", "Остаток отпуска ≤5 дней на конец периода", false,
                        List.of("VAC"),
                        List.of(new Crit("VAC_BALANCE_OK", 1, 365))),
                new AchSpec("HOST", "Ведущий", "Ведущий внутренних мероприятий", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("INTERNAL_EVENT_HOST", 1, null))),
                new AchSpec("CASE_MAKER", "Кейс-мейкер", "Разработка кейсов для хакатонов", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("HACK_CASE_AUTHOR", 1, null))),
                new AchSpec("BUGATHON", "Багатончик", "Участие в багатоне", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("BUGATHON_PARTICIPANT", 1, null))),
                new AchSpec("EDU_ARCH", "Архитектор образования", "Помощь в разработке обучающих программ", true,
                        List.of("LEARN", "COMMUNITY"),
                        List.of(new Crit("EDU_ARCH_HELP", 1, null))),
                new AchSpec("RESEARCHER", "Исследователь", "Участие в 5 фокус-группах и исследованиях", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("FOCUS_GROUP", 5, 365))),
                new AchSpec("AUTHOR_T1", "Автор Т1", "Первая статья в профильном издании", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("PRO_ARTICLE", 1, null))),
                new AchSpec("IT_CAMP", "ИТ-Лагерь", "Активное участие/помощь в проекте", true,
                        List.of("COMMUNITY"),
                        List.of(new Crit("IT_CAMP_VOL", 1, null)))
        );

        Map<String, Achievement> achByCode = new LinkedHashMap<>();
        for (AchSpec spec : specs) {
            Achievement a = Achievement.builder()
                    .code(spec.code())
                    .title(spec.title())
                    .shortDescription(spec.shortDesc())
                    .descriptionMd(spec.shortDesc())
                    .points(rnd.nextInt(6) * 10)
                    .repeatable(false)
                    .visibility(Visibility.PUBLIC)
                    .icon(icons.get(spec.code()))
                    .banner(banners.get(spec.code()))
                    .animation(animations.get(spec.code()))
                    .active(true)
                    .createdBy(sample(admins))
                    .build();

            a.setSections(spec.sectionCodes().stream().map(sections::get).collect(Collectors.toSet()));
            a = achievementRepo.save(a);
            achByCode.put(spec.code(), a);

            for (Crit c : spec.criteria()) {
                criterionRepo.save(AchievementCriterion.builder()
                        .achievement(a)
                        .activityType(act.get(c.activityCode()))
                        .requiredCount(c.required())
                        .withinDays(c.withinDays())
                        .sortOrder(100)
                        .build());
            }

            if (spec.code().equals("RESEARCHER")) {
                for (int i = 1; i <= 5; i++) {
                    stageRepo.save(AchievementStage.builder()
                            .achievement(a)
                            .stageName("Участие в исследовании #" + i)
                            .required(true)
                            .build());
                }
            } else if (spec.code().equals("PODPISANT")) {
                for (int i = 1; i <= 12; i++) {
                    stageRepo.save(AchievementStage.builder()
                            .achievement(a)
                            .stageName("Месяц без долга #" + i)
                            .required(true)
                            .build());
                }
            }
        }

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(admins);
        allUsers.addAll(users);

        List<ActivityLog> logs = new ArrayList<>();

        for (User u : allUsers) {
            int years = Period.between(u.getHireDate(), LocalDate.now()).getYears();
            years = Math.min(years, 5);
            for (int y = 1; y <= years; y++) {
                logs.add(event(u, act.get("WORK_YEAR"), daysAgo(365L * y + rnd.nextInt(60))));
            }

            if (rnd.nextDouble() < 0.6) {
                logs.add(event(u, act.get("LMS_OBLIG_CLEAR"), daysAgo(rnd.nextInt(120))));
            }

            if (rnd.nextDouble() < 0.7) {
                logs.add(event(u, act.get("PROFILE_100"), daysAgo(rnd.nextInt(60))));
            }

            int months = rnd.nextDouble() < 0.45 ? 12 : rnd.nextInt(10);
            for (int m = 0; m < months; m++) {
                logs.add(event(u, act.get("HRLINK_MONTH_CLEAR"), daysAgo(30L * m + rnd.nextInt(5))));
            }

            if (rnd.nextDouble() < 0.3) {
                logs.add(event(u, act.get("VAC_BALANCE_OK"), daysAgo(30 + rnd.nextInt(300))));
            }

            if (rnd.nextDouble() < 0.15)
                logs.add(event(u, act.get("CYBER_TOURNAMENT_WIN"), daysAgo(200 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.2)
                logs.add(event(u, act.get("CYBER_TOURNAMENT_PRIZE"), daysAgo(100 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.12)
                logs.add(event(u, act.get("MUSIC_GROUP_PERF"), daysAgo(50 + rnd.nextInt(400))));
            if (rnd.nextDouble() < 0.18)
                logs.add(event(u, act.get("INTERNAL_EVENT_HOST"), daysAgo(20 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.1) logs.add(event(u, act.get("HACK_CASE_AUTHOR"), daysAgo(60 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.2)
                logs.add(event(u, act.get("BUGATHON_PARTICIPANT"), daysAgo(40 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.08) logs.add(event(u, act.get("EDU_ARCH_HELP"), daysAgo(80 + rnd.nextInt(300))));
            int fg = rnd.nextInt(7);
            for (int i = 0; i < fg; i++) logs.add(event(u, act.get("FOCUS_GROUP"), daysAgo(10 + rnd.nextInt(600))));
            if (rnd.nextDouble() < 0.12) logs.add(event(u, act.get("PRO_ARTICLE"), daysAgo(30 + rnd.nextInt(400))));
            if (rnd.nextDouble() < 0.1) logs.add(event(u, act.get("IT_CAMP_VOL"), daysAgo(30 + rnd.nextInt(400))));
        }
        activityLogRepo.saveAll(logs);

        computeProgressAndAwards(allUsers, specs, achByCode, act);
    }


    private Map<String, Section> saveSections(Map<String, Section> map) {
        Map<String, Section> out = new LinkedHashMap<>();
        for (var e : map.entrySet()) out.put(e.getKey(), sectionRepo.save(e.getValue()));
        return out;
    }

    private Asset createAvatar(String keyPrefix) throws Exception {
        String key = "avatars/" + keyPrefix + ".png";
        byte[] bytes = storage.generateSquarePng(256, rnd.nextBoolean());
        ObjectWriteResponse resp = storage.uploadPng(bytes, key);
        return assetRepo.save(Asset.builder()
                .bucket(bucket)
                .objectKey(key)
                .versionId(Optional.ofNullable(resp.versionId()).orElse(""))
                .contentType("image/png")
                .sizeBytes((long) bytes.length)
                .etag(resp.etag())
                .build());
    }


    private Map<String, ActivityType> saveActivityTypes(List<ActivityType> list) {
        Map<String, ActivityType> out = new LinkedHashMap<>();
        for (ActivityType t : list) out.put(t.getCode(), activityTypeRepo.save(t));
        return out;
    }

    private <T> T sample(List<T> list) {
        return list.get(rnd.nextInt(list.size()));
    }

    private Instant daysAgo(long days) {
        return Instant.now().minus(Duration.ofDays(days));
    }

    private ActivityLog event(User u, ActivityType type, Instant when) {
        return ActivityLog.builder()
                .user(u)
                .activityType(type)
                .occurredAt(when)
                .sourceSystem(type.getSourceSystem() == null ? "" : type.getSourceSystem())
                .sourceEventId(UUID.randomUUID().toString())
                .payload(Map.of("note", "seed", "rand", rnd.nextInt(1000))) // любой мелкий JSON
                .build();
    }


    private void computeProgressAndAwards(List<User> users,
                                          List<AchSpec> specs,
                                          Map<String, Achievement> achByCode,
                                          Map<String, ActivityType> actByCode) {

        Map<UUID, List<ActivityLog>> logsByUser = activityLogRepo.findAll()
                .stream().collect(Collectors.groupingBy(l -> l.getUser().getId()));

        for (User u : users) {
            List<ActivityLog> userLogs = logsByUser.getOrDefault(u.getId(), List.of());

            for (AchSpec spec : specs) {
                Achievement a = achByCode.get(spec.code());

                int sumRequired = 0;
                int sumCapped = 0;

                for (Crit c : spec.criteria()) {
                    ActivityType t = actByCode.get(c.activityCode());
                    Instant threshold = (c.withinDays() == null)
                            ? Instant.EPOCH
                            : Instant.now().minus(Duration.ofDays(c.withinDays()));

                    long cnt = userLogs.stream()
                            .filter(l -> l.getActivityType().getId().equals(t.getId()))
                            .filter(l -> l.getOccurredAt().isAfter(threshold))
                            .count();

                    int required = c.required();
                    sumRequired += required;
                    sumCapped += (int) Math.min(cnt, required);
                }

                int totalSteps = Math.max(1, sumRequired);
                int currentStep = Math.min(sumCapped, totalSteps);

                boolean shouldAward = false;
                UserAchievement.Method method = spec.manual()
                        ? UserAchievement.Method.MANUAL
                        : UserAchievement.Method.AUTO;

                if (currentStep >= totalSteps) {
                    // Всегда награждаем, если шаги закрыты — даже для manual (для seed-данных это корректнее).
                    shouldAward = true;
                } else if (spec.manual()) {
                    // Случайная ручная выдача как раньше, НО теперь выравниваем прогресс
                    if (rnd.nextDouble() < 0.08) {
                        shouldAward = true;
                        currentStep = totalSteps; // выравниваем, чтобы awarded не конфликтовал со шкалой
                    }
                }

                // сохраняем прогресс уже с финальными currentStep/totalSteps
                progressRepo.save(
                        UserAchievementProgress.builder()
                                .user(u)
                                .achievement(a)
                                .currentStep(currentStep)
                                .totalSteps(totalSteps)
                                .updatedAt(Instant.now())
                                .build()
                );

                // создаём награду, если нужно
                if (shouldAward) {
                    UserAchievement.UserAchievementBuilder b = UserAchievement.builder()
                            .user(u)
                            .achievement(a)
                            .method(method)
                            .awardedAt(Instant.now().minusSeconds(rnd.nextInt(60 * 60 * 24 * 90)))
                            .evidence(Map.of("reason", method == UserAchievement.Method.AUTO
                                    ? "auto_by_criteria"
                                    : "manual_seed"));

                    if (method == UserAchievement.Method.MANUAL) {
                        // кого-то из первых админов
                        b.awardedBy(sample(userRepo.findAll().subList(0, 3)));
                    }

                    userAchRepo.save(b.build());
                }
            }
        }
    }

}

