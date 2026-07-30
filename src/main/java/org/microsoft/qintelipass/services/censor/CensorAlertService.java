package org.microsoft.qintelipass.services.censor;

import jakarta.persistence.criteria.Predicate;
import org.microsoft.qintelipass.dtos.CensorAlertDTO;
import org.microsoft.qintelipass.dtos.CensorAlertRuleDTO;
import org.microsoft.qintelipass.enums.CensorAlertStatus;
import org.microsoft.qintelipass.entity.CensorAlert;
import org.microsoft.qintelipass.entity.CensorAlertRule;
import org.microsoft.qintelipass.entity.CensorRecord;
import org.microsoft.qintelipass.repository.CensorAlertRepository;
import org.microsoft.qintelipass.repository.CensorAlertRuleRepository;
import org.microsoft.qintelipass.repository.CensorRecordRepository;
import org.microsoft.qintelipass.util.Snowflake;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CensorAlertService {

    private static final String DEFAULT_RULE_NAME = "Default sensitive word alert rule";

    private final CensorAlertRepository alertRepository;
    private final CensorAlertRuleRepository ruleRepository;
    private final CensorRecordRepository recordRepository;

    public CensorAlertService(CensorAlertRepository alertRepository,
                              CensorAlertRuleRepository ruleRepository,
                              CensorRecordRepository recordRepository) {
        this.alertRepository = alertRepository;
        this.ruleRepository = ruleRepository;
        this.recordRepository = recordRepository;
    }

    @Transactional
    public boolean evaluateAfterRecord(CensorRecord record) {
        List<CensorAlertRule> rules = enabledRules();
        boolean alertSent = false;
        for (CensorAlertRule rule : rules) {
            alertSent = evaluateRule(record, rule) || alertSent;
        }
        return alertSent;
    }

    @Transactional(readOnly = true)
    public Page<CensorAlertDTO> listAlerts(String q, String department, String status, String from, String to, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Specification<CensorAlert> spec = buildAlertSpec(q, department, status, from, to);
        return alertRepository
                .findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "triggeredAt")))
                .map(CensorAlertDTO::from);
    }

    @Transactional(readOnly = true)
    public CensorAlertDTO getAlert(Long id) {
        return alertRepository.findById(id)
                .map(CensorAlertDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
    }

    @Transactional
    public CensorAlertDTO markHandled(Long id, String handledBy) {
        CensorAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setStatus(CensorAlertStatus.HANDLED);
        alert.setCurrentCount(0);
        alert.setHandledAt(LocalDateTime.now());
        alert.setHandledBy((handledBy == null || handledBy.isBlank()) ? "admin" : handledBy.trim());
        return CensorAlertDTO.from(alertRepository.save(alert));
    }

    @Transactional
    public List<CensorAlertRuleDTO> listRules() {
        ensureDefaultRule();
        return ruleRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(CensorAlertRuleDTO::from)
                .toList();
    }

    @Transactional
    public CensorAlertRuleDTO saveRule(CensorAlertRuleDTO dto) {
        CensorAlertRule rule = dto.getId() == null
                ? new CensorAlertRule()
                : ruleRepository.findById(dto.getId()).orElse(new CensorAlertRule());
        rule.setName(defaultIfBlank(dto.getName(), DEFAULT_RULE_NAME));
        rule.setPeriodDays(Math.max(dto.getPeriodDays(), 1));
        rule.setThreshold(Math.max(dto.getThreshold(), 1));
        if (dto.getEnabled() != null) {
            rule.setEnabled(dto.getEnabled());
        } else if (dto.getId() == null) {
            rule.setEnabled(true);
        }
        rule.setCreatedBy(defaultIfBlank(dto.getCreatedBy(), "admin"));
        return CensorAlertRuleDTO.from(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> stats() {
        ensureDefaultRule();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        long todayAlerts = alertRepository.countByTriggeredAtBetween(start, end);
        long pending = alertRepository.countByStatus(CensorAlertStatus.PENDING);
        long enabledRules = ruleRepository.findByEnabledTrueOrderByCreatedAtAsc().size();
        return Map.of(
                "todayAlerts", todayAlerts,
                "pendingAlerts", pending,
                "todayNotices", todayAlerts,
                "enabledRules", enabledRules
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> notifications(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        Page<CensorAlertDTO> pendingAlerts = listAlerts(null, null, "pending", null, null, 0, safeLimit);
        return Map.of(
                "message", "您有一条新的敏感词通知",
                "pendingCount", alertRepository.countByStatus(CensorAlertStatus.PENDING),
                "items", pendingAlerts.getContent()
        );
    }

    private boolean evaluateRule(CensorRecord record, CensorAlertRule rule) {
        LocalDateTime periodStart = record.getCreatedAt().minusDays(rule.getPeriodDays() - 1L).toLocalDate().atStartOfDay();
        CensorAlert latestHandled = alertRepository
                .findFirstByUserIdAndStatusOrderByTriggeredAtDesc(record.getUserId(), CensorAlertStatus.HANDLED)
                .orElse(null);
        if (latestHandled != null && latestHandled.getHandledAt() != null && latestHandled.getHandledAt().isAfter(periodStart)) {
            periodStart = latestHandled.getHandledAt();
        }
        long triggerCount = recordRepository.countByUserIdAndAlertCountedTrueAndCreatedAtBetween(
                record.getUserId(), periodStart, record.getCreatedAt().plusNanos(1));
        if (triggerCount < rule.getThreshold()) {
            return false;
        }

        LocalDateTime dayStart = record.getCreatedAt().toLocalDate().atStartOfDay();
        LocalDateTime nextDay = dayStart.plusDays(1);
        CensorAlert existingToday = alertRepository
                .findFirstByUserIdAndTriggeredAtBetweenOrderByTriggeredAtDesc(record.getUserId(), dayStart, nextDay)
                .orElse(null);
        if (existingToday != null) {
            if (existingToday.getStatus() == CensorAlertStatus.PENDING) {
                existingToday.setCurrentCount(triggerCount);
                alertRepository.save(existingToday);
            }
            return false;
        }

        CensorAlert existingPending = alertRepository
                .findFirstByUserIdAndStatusOrderByTriggeredAtDesc(record.getUserId(), CensorAlertStatus.PENDING)
                .orElse(null);
        if (existingPending != null && existingPending.getTriggeredAt().toLocalDate().isEqual(record.getCreatedAt().toLocalDate())) {
            existingPending.setCurrentCount(triggerCount);
            alertRepository.save(existingPending);
            return false;
        }

        CensorAlert alert = new CensorAlert();
        alert.setId(Snowflake.nextId());
        alert.setUserId(record.getUserId());
        alert.setEmployeeId(String.valueOf(record.getUserId()));
        alert.setName(record.getUsername());
        alert.setDepartment(record.getDepartment());
        alert.setPosition("");
        alert.setRuleName(rule.getName());
        alert.setPeriodDays(rule.getPeriodDays());
        alert.setThreshold(rule.getThreshold());
        alert.setTriggerCount(triggerCount);
        alert.setCurrentCount(triggerCount);
        alert.setKeywords(record.getHitKeywords());
        alert.setStatus(CensorAlertStatus.PENDING);
        alert.setTriggeredAt(record.getCreatedAt());
        alert.setNoticeSentAt(record.getCreatedAt());
        alert.setEmail(null);
        alert.setContexts(toContext(record));
        alertRepository.save(alert);
        return true;
    }

    private List<CensorAlertRule> enabledRules() {
        ensureDefaultRule();
        List<CensorAlertRule> rules = ruleRepository.findByEnabledTrueOrderByCreatedAtAsc();
        if (!rules.isEmpty()) {
            return rules;
        }

        return ruleRepository.findFirstByDefaultRuleTrue().stream().toList();
    }

    private void ensureDefaultRule() {
        if (ruleRepository.findFirstByDefaultRuleTrue().isPresent()) {
            return;
        }
        CensorAlertRule rule = new CensorAlertRule();
        rule.setId(Snowflake.nextId());
        rule.setName(DEFAULT_RULE_NAME);
        rule.setPeriodDays(1);
        rule.setThreshold(3);
        rule.setEnabled(true);
        rule.setDefaultRule(true);
        rule.setCreatedBy("system");
        ruleRepository.save(rule);
    }

    private String toContext(CensorRecord record) {
        String time = record.getCreatedAt() == null ? "" : record.getCreatedAt().toLocalTime().withNano(0).toString();
        String content = (defaultIfBlank(record.getInputExcerpt(), "") + "\n" + defaultIfBlank(record.getOutputExcerpt(), "")).trim();
        return Arrays.stream(record.getHitKeywords().split(","))
                .map(keyword -> buildContext(time, keyword.trim(), content))
                .toList()
                .stream()
                .sorted(Comparator.naturalOrder())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private String buildContext(String time, String keyword, String content) {
        String before = "";
        String hit = keyword;
        String after = "";
        if (!content.isBlank() && !keyword.isBlank()) {
            int index = content.indexOf(keyword);
            if (index >= 0) {
                int beforeStart = Math.max(0, index - 40);
                int afterEnd = Math.min(content.length(), index + keyword.length() + 40);
                before = content.substring(beforeStart, index);
                after = content.substring(index + keyword.length(), afterEnd);
            } else {
                before = content.length() <= 80 ? content : content.substring(0, 80);
            }
        }
        return time + "|" + encode(keyword) + "|" + encode(before) + "|" + encode(hit) + "|" + encode(after);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(defaultIfBlank(value, "").getBytes(StandardCharsets.UTF_8));
    }

    private Specification<CensorAlert> buildAlertSpec(String q, String department, String status, String from, String to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("employeeId")), like)
                ));
            }
            if (department != null && !department.isBlank()) {
                predicates.add(cb.equal(root.get("department"), department.trim()));
            }
            if (status != null && !status.isBlank()) {
                CensorAlertStatus alertStatus = "handled".equalsIgnoreCase(status)
                        ? CensorAlertStatus.HANDLED
                        : CensorAlertStatus.PENDING;
                predicates.add(cb.equal(root.get("status"), alertStatus));
            }
            LocalDateTime fromTime = parseStartOfDay(from);
            if (fromTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("triggeredAt"), fromTime));
            }
            LocalDateTime toTime = parseStartOfNextDay(to);
            if (toTime != null) {
                predicates.add(cb.lessThan(root.get("triggeredAt"), toTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDateTime parseStartOfDay(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDate.parse(date.trim()).atStartOfDay();
    }

    private LocalDateTime parseStartOfNextDay(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDate.parse(date.trim()).plusDays(1).atStartOfDay();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
