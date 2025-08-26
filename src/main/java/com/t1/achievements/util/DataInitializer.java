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

import java.io.IOException;
import java.io.InputStream;
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

    private record Crit(String activityCode, int required, Integer withinDays) {}
    private record AchSpec(String code, String title, String shortDesc, boolean manual,
                           List<String> sectionCodes, List<Crit> criteria) {}

    private static final ZoneId Z = ZoneId.systemDefault();
    private final Random rnd = new Random(42);

    @Value("${minio.bucket}")
    private String bucket;

    // ====== Настройки для аватаров (добавь файлы в resources/sample-avatars/) ======
    private static final List<String> AVATAR_POOL = List.of(
            "sample-avatars/ru-01.jpg", "sample-avatars/ru-02.jpg", "sample-avatars/ru-03.jpg",
            "sample-avatars/ru-04.jpg", "sample-avatars/ru-05.jpg", "sample-avatars/ru-06.jpg",
            "sample-avatars/ru-07.jpg"
    );
    private int avatarCursor = 0;

    // Более реалистичные должности/отделы
    private static final List<String> POSITIONS = List.of(
            "Разработчик", "Старший разработчик", "Тестировщик", "DevOps-инженер",
            "Аналитик", "Бизнес-аналитик", "Проектный менеджер", "Дизайнер",
            "Системный администратор", "Data Engineer", "Data Scientist", "Технический писатель"
    );
    private static final List<String> DEPARTMENTS = List.of(
            "Разработка", "Тестирование", "DevOps", "Аналитика",
            "Проектный офис", "Дизайн", "HR", "Маркетинг", "Финансы", "Операции"
    );

    // 40 реальных русских имён (ФИО)
    private static final List<String> RU_FULLNAMES = List.of(
            "Иван Петров", "Мария Иванова", "Сергей Смирнов", "Анна Кузнецова",
            "Дмитрий Соколов", "Екатерина Морозова", "Андрей Попов", "Ольга Васильева",
            "Павел Новиков", "Елена Павлова", "Никита Орлов", "Татьяна Сергеева",
            "Артём Фёдоров", "Наталья Киселёва", "Игорь Алексеев", "Виктория Громова",
            "Роман Николаев", "Юлия Макарова", "Кирилл Захаров", "Светлана Богданова",
            "Михаил Комаров", "Алина Волкова", "Владимир Жуков", "Ксения Медведева",
            "Александр Емельянов", "Дарья Никитина", "Константин Беляев", "Полина Сафонова",
            "Григорий Романов", "Анастасия Карпова", "Тимур Васильев", "Валерия Белова",
            "Денис Гусев", "Вероника Фролова", "Степан Калинин", "Ирина Сорокина",
            "Максим Ковалёв", "Лилия Антонова", "Евгений Данилов", "Наталия Кузьмина"
    );

    private Role upsertRole(String code, String name) {
        return roleRepo.findByCode(code)
                .orElseGet(() -> roleRepo.save(Role.builder().code(code).name(name).build()));
    }

    @PostConstruct
    @Transactional
    public void init() throws Exception {
        if (userRepo.count() > 0 || achievementRepo.count() > 0) return;

        Role admin = upsertRole("ADMIN", "Администратор");
        Role userRole = upsertRole("USER", "Пользователь");

        String hashed = passwordEncoder.encode("password");

        // ====== 1 админ с реальным именем ======
        List<User> admins = new ArrayList<>();
        String adminName = "Алексей Кузнецов";
        String adminUsername = "admin";
        admins.add(userRepo.save(
                User.builder()
                        .username(adminUsername)
                        .password(hashed)
                        .fullName(adminName)
                        .email(adminUsername + "@t1.local")
                        .position("Администратор систем")
                        .department("IT")
                        .grade(6)
                        .active(true)
                        .role(admin)
                        .hireDate(LocalDate.now().minusYears(5))
                        .phone(randomPhone())
                        .avatar(uploadAvatarFromPool("admin"))
                        .build()
        ));

        // ====== 40 обычных пользователей с русскими именами ======
        List<User> users = new ArrayList<>();
        int idx = 1;
        for (String fullName : RU_FULLNAMES) {
            String username = toUsername(fullName);
            users.add(userRepo.save(
                    User.builder()
                            .username(username)
                            .password(hashed)
                            .fullName(fullName)
                            .email(username + "@t1.local")
                            .position(sample(POSITIONS))
                            .department(sample(DEPARTMENTS))
                            .grade(1 + rnd.nextInt(6)) // 1..6
                            .active(true)
                            .role(userRole)
                            .hireDate(LocalDate.now().minusDays(220 + rnd.nextInt(1400)))
                            .phone(randomPhone())
                            .avatar(uploadAvatarFromPool("user%02d".formatted(idx++)))
                            .build()
            ));
        }

        Map<String, Section> sections = saveSections(
                Map.of(
                        "BASE",     new Section(null, "BASE",     "Это база", "Обязательные основы", 10, true, Instant.now()),
                        "SPORT",    new Section(null, "SPORT",    "Спорт", "Киберспорт и активность", 20, true, Instant.now()),
                        "SPEAK",    new Section(null, "SPEAK",    "Выступление на конференциях, преподавание, фасилитации",
                                "Публичные выступления, наставничество, фасилитация", 30, true, Instant.now()),
                        "EXPERT",   new Section(null, "EXPERT",   "Экспертиза", "Кейсы, багатоны, образование, исследования", 40, true, Instant.now()),
                        "AUTHOR",   new Section(null, "AUTHOR",   "Автор публикаций", "Статьи и публикации", 50, true, Instant.now()),
                        "PROJECTS", new Section(null, "PROJECTS", "Участие в проектах", "Волонтёрство и спец-проекты", 60, true, Instant.now())
                )
        );

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
                        new ActivityType(null, "IT_CAMP_VOL", "Волонтёр ИТ-Лагеря", null, "MANUAL", true, Instant.now()),
                        new ActivityType(null,"TEST_1_PASSED","пройден тест 1",null,"AUTO",true,Instant.now()),
                        new ActivityType(null,"TEST_2_PASSED","пройден тест 2",null,"AUTO",true,Instant.now())
                )
        );

        List<AchSpec> specs = List.of(
                new AchSpec("ZNATOK",       "Знаток",             "Нет просроченных обязательных курсов", false,
                        List.of("BASE"),     List.of(new Crit("LMS_OBLIG_CLEAR", 1, null))),
                new AchSpec("PODPISANT",    "Подписант",          "12 месяцев подряд без долга по подписям", false,
                        List.of("BASE"),     List.of(new Crit("HRLINK_MONTH_CLEAR", 12, 365))),
                new AchSpec("PROFILE_100",  "Идеальный профиль",  "100% заполненный профиль", false,
                        List.of("BASE"),     List.of(new Crit("PROFILE_100", 1, null))),
                new AchSpec("TEAM_YEAR_1",  "Год в команде 1",    "полный год в компании", false,
                        List.of("BASE"),     List.of(new Crit("WORK_YEAR", 2, null))),
                new AchSpec("VAC_BALANCE",  "В балансе",          "Остаток отпуска ≤5 дней на конец периода", false,
                        List.of("BASE"),     List.of(new Crit("VAC_BALANCE_OK", 1, 365))),

                new AchSpec("CYBER_CHAMPION","Кибер чемпион",     "Победа в кибертурнире", true,
                        List.of("SPORT"),    List.of(new Crit("CYBER_TOURNAMENT_WIN", 1, null))),
                new AchSpec("CYBER_MASTER", "Кибер мастер",       "Призовое место в кибертурнире", true,
                        List.of("SPORT"),    List.of(new Crit("CYBER_TOURNAMENT_PRIZE", 1, null))),
                new AchSpec("MUZYKANT1",    "МузыканТ1",          "Участие в музыкальных группах и выступлениях", true,
                        List.of("SPORT"),    List.of(new Crit("MUSIC_GROUP_PERF", 1, null))),

                new AchSpec("HOST",         "Ведущий",            "Ведущий внутренних мероприятий", true,
                        List.of("SPEAK"),    List.of(new Crit("INTERNAL_EVENT_HOST", 1, null))),

                new AchSpec("CASE_MAKER",   "Кейс-мейкер",        "Разработка кейсов для хакатонов", true,
                        List.of("EXPERT"),   List.of(new Crit("HACK_CASE_AUTHOR", 1, null))),
                new AchSpec("BUGATHON",     "Багатончик",         "Участие в багатоне", true,
                        List.of("EXPERT"),   List.of(new Crit("BUGATHON_PARTICIPANT", 1, null))),
                new AchSpec("EDU_ARCH",     "Архитектор образования", "Помощь в разработке обучающих программ", true,
                        List.of("EXPERT"),   List.of(new Crit("EDU_ARCH_HELP", 1, null))),
                new AchSpec("RESEARCHER",   "Исследователь",      "Участие в 5 фокус-группах и исследованиях", true,
                        List.of("EXPERT"),   List.of(new Crit("FOCUS_GROUP", 5, 365))),

                new AchSpec("AUTHOR_T1",    "Автор Т1",           "Первая статья в профильном издании", true,
                        List.of("AUTHOR"),   List.of(new Crit("PRO_ARTICLE", 1, null))),

                new AchSpec("IT_CAMP",      "ИТ-Лагерь",          "Активное участие/помощь в проекте", true,
                        List.of("PROJECTS"), List.of(new Crit("IT_CAMP_VOL", 1, null)))
        );

        Map<String, Achievement> achByCode = new LinkedHashMap<>();
        for (AchSpec spec : specs) {
            String code = spec.code();

            byte[] pngBytes = readResourceRequired(code + ".png");
            byte[] gifBytes = readResourceRequired(code + ".gif");

            Asset icon = uploadAndSaveAsset(pngBytes, "icons/" + code + ".png", "image/png");
            Asset banner = uploadAndSaveAsset(pngBytes, "banners/" + code + ".png", "image/png");
            Asset animation = uploadAndSaveAsset(gifBytes, "animations/" + code + ".gif", "image/gif");

            Achievement a = Achievement.builder()
                    .code(code)
                    .title(spec.title())
                    .shortDescription(spec.shortDesc())
                    .descriptionMd(spec.shortDesc())
                    .points(rnd.nextInt(6) * 10)
                    .repeatable(false)
                    .visibility(Visibility.PUBLIC)
                    .icon(icon)
                    .banner(banner)
                    .animation(animation)
                    .active(true)
                    .createdBy(sample(admins))
                    .build();

            a.setSections(spec.sectionCodes().stream().map(sections::get).collect(Collectors.toSet()));
            a = achievementRepo.save(a);
            achByCode.put(code, a);

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

            if (rnd.nextDouble() < 0.3)  logs.add(event(u, act.get("VAC_BALANCE_OK"), daysAgo(30 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.15) logs.add(event(u, act.get("CYBER_TOURNAMENT_WIN"), daysAgo(200 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.2)  logs.add(event(u, act.get("CYBER_TOURNAMENT_PRIZE"), daysAgo(100 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.12) logs.add(event(u, act.get("MUSIC_GROUP_PERF"), daysAgo(50 + rnd.nextInt(400))));
            if (rnd.nextDouble() < 0.18) logs.add(event(u, act.get("INTERNAL_EVENT_HOST"), daysAgo(20 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.1)  logs.add(event(u, act.get("HACK_CASE_AUTHOR"), daysAgo(60 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.2)  logs.add(event(u, act.get("BUGATHON_PARTICIPANT"), daysAgo(40 + rnd.nextInt(300))));
            if (rnd.nextDouble() < 0.08) logs.add(event(u, act.get("EDU_ARCH_HELP"), daysAgo(80 + rnd.nextInt(300))));
            int fg = rnd.nextInt(7);
            for (int i = 0; i < fg; i++) logs.add(event(u, act.get("FOCUS_GROUP"), daysAgo(10 + rnd.nextInt(600))));
            if (rnd.nextDouble() < 0.12) logs.add(event(u, act.get("PRO_ARTICLE"), daysAgo(30 + rnd.nextInt(400))));
            if (rnd.nextDouble() < 0.1)  logs.add(event(u, act.get("IT_CAMP_VOL"), daysAgo(30 + rnd.nextInt(400))));
        }
        activityLogRepo.saveAll(logs);

        computeProgressAndAwards(allUsers, specs, achByCode, act);
    }

    private Map<String, Section> saveSections(Map<String, Section> map) {
        Map<String, Section> out = new LinkedHashMap<>();
        for (var e : map.entrySet()) out.put(e.getKey(), sectionRepo.save(e.getValue()));
        return out;
    }

    // Загружаем аватар из пула ресурсов; если файла нет — генерируем PNG-квадрат
    private Asset uploadAvatarFromPool(String keyPrefix) throws Exception {
        String resPath = AVATAR_POOL.get(avatarCursor++ % AVATAR_POOL.size());
        byte[] bytes = readResourceIfExists(resPath);
        String ext = (resPath.endsWith(".png") ? "png" : "jpg");
        String objectKey;
        String contentType;
        ObjectWriteResponse resp;
        if (bytes != null) {
            objectKey = "avatars/" + keyPrefix + "." + ext;
            contentType = ("png".equals(ext) ? "image/png" : "image/jpeg");
            resp = storage.upload(bytes, objectKey, contentType);
        } else {
            objectKey = "avatars/" + keyPrefix + ".png";
            contentType = "image/png";
            byte[] png = storage.generateSquarePng(256, rnd.nextBoolean());
            resp = storage.uploadPng(png, objectKey);
            bytes = (bytes == null ? storage.generateSquarePng(256, false) : bytes);
            bytes = png; // для сохранения sizeBytes
        }
        return assetRepo.save(Asset.builder()
                .bucket(bucket)
                .objectKey(objectKey)
                .versionId(Optional.ofNullable(resp.versionId()).orElse(""))
                .contentType(contentType)
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
                    shouldAward = true;
                } else if (spec.manual()) {
                    if (rnd.nextDouble() < 0.08) {
                        shouldAward = true;
                        currentStep = totalSteps;
                    }
                }

                progressRepo.save(
                        UserAchievementProgress.builder()
                                .user(u)
                                .achievement(a)
                                .currentStep(currentStep)
                                .totalSteps(totalSteps)
                                .updatedAt(Instant.now())
                                .build()
                );

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
                        b.awardedBy(userRepo.findAll().get(0)); // единственный админ
                    }

                    userAchRepo.save(b.build());
                }
            }
        }
    }

    private byte[] readResourceRequired(String resourceName) throws IOException {
        ClassPathResource res = new ClassPathResource(resourceName);
        if (!res.exists()) {
            throw new IOException("Не найден ресурс в classpath: " + resourceName +
                    " (ожидался, т.к. используется для замены заглушек)");
        }
        try (InputStream in = res.getInputStream()) {
            return in.readAllBytes();
        }
    }

    private byte[] readResourceIfExists(String resourceName) {
        try {
            ClassPathResource res = new ClassPathResource(resourceName);
            if (!res.exists()) return null;
            try (InputStream in = res.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private String randomPhone() {
        int p1 = 100 + rnd.nextInt(900);
        int p2 = 100 + rnd.nextInt(900);
        int p3 = 10 + rnd.nextInt(90);
        int p4 = 10 + rnd.nextInt(90);
        return String.format("+7 %03d %03d %02d-%02d", p1, p2, p3, p4);
    }

    private Asset uploadAndSaveAsset(byte[] bytes, String objectKey, String contentType) throws Exception {
        ObjectWriteResponse resp;
        if ("image/png".equals(contentType)) {
            resp = storage.uploadPng(bytes, objectKey);
        } else {
            resp = storage.upload(bytes, objectKey, contentType);
        }
        return assetRepo.save(Asset.builder()
                .bucket(bucket)
                .objectKey(objectKey)
                .versionId(Optional.ofNullable(resp.versionId()).orElse(""))
                .contentType(contentType)
                .sizeBytes((long) bytes.length)
                .etag(resp.etag())
                .build());
    }

    // ====== Транслитерация ФИО -> username/email (ivan.petrov) ======
    private String toUsername(String fullNameRu) {
        String[] parts = fullNameRu.trim().split("\\s+");
        String base = (parts.length >= 2 ? parts[0] + "." + parts[1] : fullNameRu).toLowerCase(Locale.ROOT);
        return translit(base).replaceAll("[^a-z0-9.]", "");
    }

    private String translit(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (char ch : s.toCharArray()) {
            switch (Character.toLowerCase(ch)) {
                case 'а' -> sb.append("a");
                case 'б' -> sb.append("b");
                case 'в' -> sb.append("v");
                case 'г' -> sb.append("g");
                case 'д' -> sb.append("d");
                case 'е' -> sb.append("e");
                case 'ё' -> sb.append("yo");
                case 'ж' -> sb.append("zh");
                case 'з' -> sb.append("z");
                case 'и' -> sb.append("i");
                case 'й' -> sb.append("y");
                case 'к' -> sb.append("k");
                case 'л' -> sb.append("l");
                case 'м' -> sb.append("m");
                case 'н' -> sb.append("n");
                case 'о' -> sb.append("o");
                case 'п' -> sb.append("p");
                case 'р' -> sb.append("r");
                case 'с' -> sb.append("s");
                case 'т' -> sb.append("t");
                case 'у' -> sb.append("u");
                case 'ф' -> sb.append("f");
                case 'х' -> sb.append("h");
                case 'ц' -> sb.append("ts");
                case 'ч' -> sb.append("ch");
                case 'ш' -> sb.append("sh");
                case 'щ' -> sb.append("sch");
                case 'ъ' -> {}
                case 'ы' -> sb.append("y");
                case 'ь' -> {}
                case 'э' -> sb.append("e");
                case 'ю' -> sb.append("yu");
                case 'я' -> sb.append("ya");
                default -> sb.append(ch);
            }
        }
        return sb.toString();
    }
}
