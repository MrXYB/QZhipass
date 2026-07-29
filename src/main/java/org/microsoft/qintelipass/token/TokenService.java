package org.microsoft.qintelipass.token;

import jakarta.annotation.PostConstruct;
import org.microsoft.qintelipass.enums.UserStatus;
import org.microsoft.qintelipass.models.User;
import org.microsoft.qintelipass.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Token 用量统计与限额管理服务。
 */
@Service
public class TokenService {

    private static final String GLOBAL_QUOTA_KEY = "global_token_quota";
    private static final String USER_QUOTA_KEY_PREFIX = "user_quota_";
    private static final long DEFAULT_QUOTA = 100_000L;

    private final TokenUsageRepository tokenUsageRepository;
    private final GlobalConfigRepository globalConfigRepository;
    private final UserRepository userRepository;

    public TokenService(
            TokenUsageRepository tokenUsageRepository,
            GlobalConfigRepository globalConfigRepository,
            UserRepository userRepository
    ) {
        this.tokenUsageRepository = tokenUsageRepository;
        this.globalConfigRepository = globalConfigRepository;
        this.userRepository = userRepository;
    }

    /**
     * 应用启动时写入默认全局配额。
     */
    @PostConstruct
    public void initDefaultQuota() {
        if (globalConfigRepository.findById(GLOBAL_QUOTA_KEY).isEmpty()) {
            globalConfigRepository.save(new GlobalConfig(
                    GLOBAL_QUOTA_KEY,
                    String.valueOf(DEFAULT_QUOTA)
            ));
        }
    }

    // ==================== 限额管理 ====================

    public long getGlobalQuota() {
        return globalConfigRepository.findById(GLOBAL_QUOTA_KEY)
                .map(GlobalConfig::getValue)
                .map(value -> parseQuota(value, DEFAULT_QUOTA))
                .orElse(DEFAULT_QUOTA);
    }

    /**
     * 获取用户配额；个人配额不存在时使用全局配额。
     */
    public long getUserQuota(Long userId) {
        if (userId == null) {
            return getGlobalQuota();
        }

        return globalConfigRepository.findById(USER_QUOTA_KEY_PREFIX + userId)
                .map(GlobalConfig::getValue)
                .map(value -> parseQuota(value, getGlobalQuota()))
                .orElseGet(this::getGlobalQuota);
    }

    /**
     * 管理员统一设置所有用户的 Token 限额。
     */
    public void setGlobalQuota(long quota) {
        validateQuota(quota);
        globalConfigRepository.save(new GlobalConfig(
                GLOBAL_QUOTA_KEY,
                String.valueOf(quota)
        ));
    }

    /**
     * 管理员为某个用户设置独立配额。
     */
    public void setUserQuota(Long userId, long quota) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        validateQuota(quota);
        globalConfigRepository.save(new GlobalConfig(
                USER_QUOTA_KEY_PREFIX + userId,
                String.valueOf(quota)
        ));
    }

    /**
     * 保留原调用入口。
     */
    public void setGlobalQuotaWithCount(long quota) {
        setGlobalQuota(quota);
    }

    // ==================== 用量记录 ====================

    /**
     * 按用户、自然日和模型累加一次聊天产生的 Token 消耗。
     */
    @Transactional
    public void recordUsage(
            Long userId,
            String model,
            long promptTokens,
            long completionTokens
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (promptTokens < 0 || completionTokens < 0) {
            throw new IllegalArgumentException("Token usage must not be negative");
        }

        LocalDate today = LocalDate.now();
        TokenUsage usage = tokenUsageRepository
                .findByUserIdAndUsageDateAndModel(userId, today, model)
                .orElseGet(TokenUsage::new);

        if (usage.getId() == null) {
            usage.setUserId(userId);
            usage.setUsageDate(today);
            usage.setModel(model);
        }

        usage.setPromptTokens(usage.getPromptTokens() + promptTokens);
        usage.setCompletionTokens(usage.getCompletionTokens() + completionTokens);
        usage.setTotalTokens(usage.getTotalTokens() + promptTokens + completionTokens);
        tokenUsageRepository.save(usage);
    }

    // ==================== 状态查询 ====================

    /**
     * 获取某用户当日限额与使用状态。
     */
    public UserTokenStatus getDailyStatus(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        long quota = getUserQuota(userId);
        List<TokenUsage> usageList = tokenUsageRepository
                .findByUserIdAndUsageDate(userId, LocalDate.now());
        long used = usageList.stream()
                .mapToLong(TokenUsage::getTotalTokens)
                .sum();

        User user = userRepository.findById(userId).orElse(null);
        String department = user == null ? null : user.getDepartment();
        String userName = user == null ? null : user.getName();

        return new UserTokenStatus(
                userId,
                quota,
                used,
                Math.max(0, quota - used),
                used >= quota,
                department,
                userName
        );
    }

    /**
     * 检查本次预计消耗是否仍在用户配额以内。
     */
    public boolean checkQuota(Long userId, long estimatedTokens) {
        if (estimatedTokens < 0) {
            throw new IllegalArgumentException("Estimated tokens must not be negative");
        }
        UserTokenStatus status = getDailyStatus(userId);
        return status.used() + estimatedTokens <= status.quota();
    }

    // ==================== 管理员看板 ====================

    /**
     * 活跃用户、超额用户和近七天分模型消耗。
     */
    public DashboardData getDashboard() {
        long quota = getGlobalQuota();
        LocalDate today = LocalDate.now();

        Map<Long, Long> todayUsageByUser = sumUsageByUser(
                tokenUsageRepository.findByUsageDate(today)
        );
        long overQuotaUsers = todayUsageByUser.entrySet().stream()
                .filter(entry -> entry.getValue() >= getUserQuota(entry.getKey()))
                .count();

        LocalDate start = today.minusDays(6);
        List<String> dates = new ArrayList<>();
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            dates.add(today.minusDays(daysAgo).toString());
        }

        Map<String, List<Long>> models = new LinkedHashMap<>();
        List<Long> totals = new ArrayList<>(Collections.nCopies(7, 0L));
        for (TokenUsageRepository.ModelDailyTotal dailyTotal
                : tokenUsageRepository.findDailyTotalsSince(start)) {
            List<Long> modelTotals = models.computeIfAbsent(
                    dailyTotal.getModel(),
                    ignored -> new ArrayList<>(Collections.nCopies(7, 0L))
            );
            int index = (int) ChronoUnit.DAYS.between(
                    start,
                    dailyTotal.getUsageDate()
            );
            if (index >= 0 && index < 7) {
                long value = dailyTotal.getTotal() == null ? 0L : dailyTotal.getTotal();
                modelTotals.set(index, value);
                totals.set(index, totals.get(index) + value);
            }
        }

        return new DashboardData(
                todayUsageByUser.size(),
                overQuotaUsers,
                quota,
                dates,
                models,
                totals
        );
    }

    /**
     * 当日按部门汇总，同时返回员工用量明细。
     */
    public DepartmentUsageData getDepartmentUsage() {
        LocalDate today = LocalDate.now();
        Map<Long, Long> usageByUser = sumUsageByUser(
                tokenUsageRepository.findByUsageDate(today)
        );

        Map<String, long[]> departmentAggregates = new LinkedHashMap<>();
        List<DepartmentUsageData.UserUsageRow> userRows = new ArrayList<>();

        for (User user : getActiveUsers()) {
            long used = usageByUser.getOrDefault(user.getId(), 0L);
            long quota = getUserQuota(user.getId());
            boolean overQuota = used >= quota;
            String department = normalizeDepartment(user.getDepartment());

            userRows.add(new DepartmentUsageData.UserUsageRow(
                    user.getId(),
                    user.getName(),
                    department,
                    used,
                    quota,
                    overQuota
            ));

            long[] aggregate = departmentAggregates.computeIfAbsent(
                    department,
                    ignored -> new long[3]
            );
            aggregate[0]++;
            aggregate[1] += used;
            if (overQuota) {
                aggregate[2]++;
            }
        }

        List<DepartmentUsageData.DepartmentRow> departmentRows =
                departmentAggregates.entrySet().stream()
                        .map(entry -> new DepartmentUsageData.DepartmentRow(
                                entry.getKey(),
                                entry.getValue()[0],
                                entry.getValue()[1],
                                entry.getValue()[2]
                        ))
                        .sorted(Comparator.comparing(
                                DepartmentUsageData.DepartmentRow::department
                        ))
                        .toList();

        userRows.sort(
                Comparator.comparing(DepartmentUsageData.UserUsageRow::department)
                        .thenComparing(
                                DepartmentUsageData.UserUsageRow::totalTokens,
                                Comparator.reverseOrder()
                        )
        );

        return new DepartmentUsageData(
                today.toString(),
                departmentRows,
                userRows
        );
    }

    // ==================== 员工趋势和记录 ====================

    /**
     * 用户近七天每日消耗趋势。
     */
    public Map<String, Object> getUserWeeklyTrend(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        Map<LocalDate, Long> usageByDate = tokenUsageRepository
                .findByUserIdAndUsageDateBetween(userId, start, today)
                .stream()
                .collect(Collectors.groupingBy(
                        TokenUsage::getUsageDate,
                        Collectors.summingLong(TokenUsage::getTotalTokens)
                ));

        String[] weekdays = {
                "周日", "周一", "周二", "周三", "周四", "周五", "周六"
        };
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        long total = 0;
        int activeDays = 0;

        for (LocalDate date = start;
                !date.isAfter(today);
                date = date.plusDays(1)) {
            labels.add(String.format(
                    "%02d-%02d %s",
                    date.getMonthValue(),
                    date.getDayOfMonth(),
                    weekdays[date.getDayOfWeek().getValue() % 7]
            ));

            long value = usageByDate.getOrDefault(date, 0L);
            data.add(value);
            total += value;
            if (value > 0) {
                activeDays++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        result.put("monthlyTotal", total);
        result.put("averageDaily", activeDays == 0 ? 0 : total / activeDays);
        result.put("activeDays", activeDays);
        return result;
    }

    /**
     * 用户最近三天的模型用量记录。
     */
    public List<Map<String, Object>> getRecentConversations(Long userId) {
        LocalDate today = LocalDate.now();
        return tokenUsageRepository
                .findByUserIdAndUsageDateBetween(
                        userId,
                        today.minusDays(3),
                        today
                )
                .stream()
                .sorted(Comparator.comparing(
                        TokenUsage::getUsageDate,
                        Comparator.reverseOrder()
                ))
                .limit(20)
                .map(this::toConversationRow)
                .toList();
    }

    // ==================== 前端管理员看板适配 ====================

    /**
     * 生成 admin-token-dashboard 页面所需的数据结构。
     */
    public Map<String, Object> getDashboardForFrontend() {
        LocalDate today = LocalDate.now();
        long globalQuota = getGlobalQuota();
        Map<Long, Long> usageByUser = sumUsageByUser(
                tokenUsageRepository.findByUsageDate(today)
        );

        long overQuotaUsers = usageByUser.entrySet().stream()
                .filter(entry -> entry.getValue() >= getUserQuota(entry.getKey()))
                .count();
        long todayTotal = usageByUser.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeUsers", usageByUser.size());
        result.put("overQuotaUsers", overQuotaUsers);
        result.put("todayTotalConsumption", todayTotal);
        result.put("chartData", buildFrontendChart(today));
        result.put("employees", buildEmployeeListForFrontend(usageByUser));
        result.put("globalLimit", globalQuota);
        return result;
    }

    public long countActiveUsers() {
        return getActiveUsers().size();
    }

    public List<TokenUsage> findByUserIdAndUsageDateBetween(
            Long userId,
            LocalDate start,
            LocalDate end
    ) {
        return tokenUsageRepository.findByUserIdAndUsageDateBetween(
                userId,
                start,
                end
        );
    }

    // ==================== 演示数据 ====================

    /**
     * 数据库没有 Token 用量时，按现有用户生成演示数据。
     */
    @PostConstruct
    public void seedDemoData() {
        try {
            if (tokenUsageRepository.count() > 0) {
                return;
            }
        } catch (RuntimeException exception) {
            return;
        }

        List<User> users = getActiveUsers();
        if (users.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        Random random = new Random(42);
        String[] models = {"千问", "DeepSeek", "Llama-3.1"};
        Set<String> seen = new HashSet<>();

        for (User user : users) {
            for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
                LocalDate date = today.minusDays(daysAgo);
                int expectedRecords = random.nextInt(3) + 2;
                int createdRecords = 0;

                for (int attempt = 0;
                        attempt < expectedRecords + 2
                                && createdRecords < expectedRecords;
                        attempt++) {
                    String model = models[random.nextInt(models.length)];
                    String uniqueKey = user.getId() + "-" + date + "-" + model;
                    if (!seen.add(uniqueKey)) {
                        continue;
                    }

                    long tokens = random.nextInt(50_000) + 5_000L;
                    TokenUsage usage = new TokenUsage();
                    usage.setUserId(user.getId());
                    usage.setUsageDate(date);
                    usage.setModel(model);
                    usage.setPromptTokens(tokens / 2);
                    usage.setCompletionTokens(tokens - tokens / 2);
                    usage.setTotalTokens(tokens);

                    try {
                        tokenUsageRepository.save(usage);
                        createdRecords++;
                    } catch (RuntimeException ignored) {
                        // 唯一键冲突时跳过该条演示数据。
                    }
                }
            }
        }
    }

    // ==================== 定时清理 ====================

    /**
     * 每日零点删除 30 天以前的记录。
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void dailyReset() {
        tokenUsageRepository.deleteByUsageDateBefore(
                LocalDate.now().minusDays(30)
        );
    }

    private Map<Long, Long> sumUsageByUser(List<TokenUsage> usageList) {
        return usageList.stream()
                .collect(Collectors.groupingBy(
                        TokenUsage::getUserId,
                        Collectors.summingLong(TokenUsage::getTotalTokens)
                ));
    }

    private Map<String, Object> buildFrontendChart(LocalDate today) {
        LocalDate start = today.minusDays(6);
        String[] weekdays = {
                "周日", "周一", "周二", "周三", "周四", "周五", "周六"
        };
        List<String> labels = new ArrayList<>();
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate date = today.minusDays(daysAgo);
            labels.add(String.format(
                    "%02d-%02d %s",
                    date.getMonthValue(),
                    date.getDayOfMonth(),
                    weekdays[date.getDayOfWeek().getValue() % 7]
            ));
        }

        Map<String, Map<LocalDate, Long>> usageByModelAndDate =
                new LinkedHashMap<>();
        for (TokenUsageRepository.ModelDailyTotal total
                : tokenUsageRepository.findDailyTotalsSince(start)) {
            usageByModelAndDate
                    .computeIfAbsent(
                            total.getModel(),
                            ignored -> new LinkedHashMap<>()
                    )
                    .put(
                            total.getUsageDate(),
                            total.getTotal() == null ? 0L : total.getTotal()
                    );
        }

        List<String> modelNames = new ArrayList<>(
                List.of("千问", "DeepSeek", "Llama-3.1")
        );
        usageByModelAndDate.keySet().stream()
                .filter(model -> !modelNames.contains(model))
                .sorted()
                .forEach(modelNames::add);

        List<Map<String, Object>> datasets = new ArrayList<>();
        for (String model : modelNames) {
            Map<LocalDate, Long> usageByDate = usageByModelAndDate
                    .getOrDefault(model, Map.of());
            List<Long> data = new ArrayList<>();
            for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
                data.add(usageByDate.getOrDefault(
                        today.minusDays(daysAgo),
                        0L
                ));
            }

            Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put("label", model);
            dataset.put("data", data);
            datasets.add(dataset);
        }

        Map<String, Object> chartData = new LinkedHashMap<>();
        chartData.put("labels", labels);
        chartData.put("datasets", datasets);
        return chartData;
    }

    private List<Map<String, Object>> buildEmployeeListForFrontend(
            Map<Long, Long> usageByUser
    ) {
        return getActiveUsers().stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .map(user -> {
                    long used = usageByUser.getOrDefault(user.getId(), 0L);
                    long quota = getUserQuota(user.getId());

                    Map<String, Object> employee = new LinkedHashMap<>();
                    employee.put("id", String.valueOf(user.getId()));
                    employee.put("name", user.getName());
                    employee.put(
                            "department",
                            normalizeDepartment(user.getDepartment())
                    );
                    employee.put("totalTokens", used);
                    employee.put("quota", quota);
                    employee.put("overQuota", used >= quota);
                    return employee;
                })
                .toList();
    }

    private Map<String, Object> toConversationRow(TokenUsage usage) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", usage.getId());
        row.put("modelName", usage.getModel());
        row.put("tokensUsed", usage.getTotalTokens());
        row.put("usageDate", usage.getUsageDate().toString());
        row.put("content", "对话记录");
        row.put("createdAt", usage.getUsageDate().toString());
        return row;
    }

    private List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() != UserStatus.DEACTIVATED)
                .toList();
    }

    private String normalizeDepartment(String department) {
        return department == null || department.isBlank()
                ? "未分配"
                : department;
    }

    private long parseQuota(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private void validateQuota(long quota) {
        if (quota < 0) {
            throw new IllegalArgumentException(
                    "Token quota must not be negative"
            );
        }
    }
}
