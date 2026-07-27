package org.microsoft.qintelipass.services;

import org.microsoft.qintelipass.dtos.CensorRecordDTO;
import org.microsoft.qintelipass.models.CensorKeyword;
import org.microsoft.qintelipass.models.CensorRecord;
import org.microsoft.qintelipass.repository.CensorKeywordRepository;
import org.microsoft.qintelipass.repository.CensorRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CensorService {

    private final CensorKeywordRepository censorKeywordRepository;
    private final CensorRecordRepository censorRecordRepository;
    private final VectorCensorService vectorCensorService;
    private final CensorAlertService censorAlertService;

    public CensorService(CensorKeywordRepository censorKeywordRepository,
                        CensorRecordRepository censorRecordRepository,
                        VectorCensorService vectorCensorService,
                        CensorAlertService censorAlertService) {
        this.censorKeywordRepository = censorKeywordRepository;
        this.censorRecordRepository = censorRecordRepository;
        this.vectorCensorService = vectorCensorService;
        this.censorAlertService = censorAlertService;
    }

    @Transactional(readOnly = true)
    public List<CensorKeyword> listKeywords() {
        return censorKeywordRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public CensorKeyword addKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword must not be blank.");
        }

        return censorKeywordRepository.save(new CensorKeyword(keyword.trim()));
    }

    @Transactional
    public CensorKeyword setKeywordEnabled(Long id, boolean enabled) {
        CensorKeyword keyword = censorKeywordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Censor keyword does not exist."));

        keyword.setEnabled(enabled);
        return censorKeywordRepository.save(keyword);
    }

    @Transactional(readOnly = true)
    public List<CensorRecord> listRecords() {
        return (List<CensorRecord>) censorRecordRepository.findAllByOrderByCreatedAtDesc(Pageable.ofSize(1));
    }

    
    @Transactional
    public void checkAndRecord(Long userId,
                               String username,
                               String phone,
                               String department,
                               String modelName,
                               String inputContent,
                               String outputContent) {
        String fullContent = "";

        if (inputContent != null) {
            fullContent += inputContent;
        }

        if (outputContent != null) {
            fullContent += "\n" + outputContent;
        }

        List<String> exactHits = findExactHitKeywords(fullContent);
        List<String> vectorHits = vectorCensorService.findSimilarSensitiveWords(fullContent);

        Set<String> allHits = new LinkedHashSet<>();
        allHits.addAll(exactHits);
        allHits.addAll(vectorHits);

        if (allHits.isEmpty()) {
            return;
        }

        boolean alertCounted = shouldCountForAlert(userId);
        CensorRecord record = new CensorRecord(
                userId,
                username,
                phone,
                department,
                modelName,
                String.join(",", allHits),
                excerpt(inputContent),
                excerpt(outputContent)
        );
        record.setAlertCounted(alertCounted);

        CensorRecord savedRecord = censorRecordRepository.save(record);
        incrementKeywordTriggerCounts(allHits);
        if (alertCounted) {
            boolean alertSent = censorAlertService.evaluateAfterRecord(savedRecord);
            savedRecord.setAdminNotified(alertSent);
        }
    }

    private void incrementKeywordTriggerCounts(Set<String> hitKeywords) {
        for (String hitKeyword : hitKeywords) {
            censorKeywordRepository.findByKeyword(hitKeyword).ifPresent(keyword -> {
                keyword.setTriggerCount(keyword.getTriggerCount() + 1);
                censorKeywordRepository.save(keyword);
            });
        }
    }

    private boolean shouldCountForAlert(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minuteStart = now.withSecond(0).withNano(0);
        LocalDateTime nextMinute = minuteStart.plusMinutes(1);
        return !censorRecordRepository.existsByUserIdAndAlertCountedTrueAndCreatedAtBetween(
                userId,
                minuteStart,
                nextMinute
        );
    }

    private String excerpt(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String normalized = content.trim();
        return normalized.length() <= 1000 ? normalized : normalized.substring(0, 1000);
    }

    @Transactional(readOnly = true)
    public Page<CensorRecordDTO> listAllRecords(int page, int size) {
        return censorRecordRepository.findAllByOrderByCreatedAtDesc(Pageable.ofSize(size).withPage(page))
                .map(CensorRecordDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<CensorRecordDTO> searchRecords(String query, int page, int size) {
        return censorRecordRepository
                .findByUsernameContainingOrHitKeywordsContainingAllIgnoreCaseOrderByCreatedAtDesc(
                        query, query, PageRequest.of(page, size))
                .map(CensorRecordDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<CensorRecordDTO> listRecordsByUser(Long userId, int page, int size) {
        return censorRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(CensorRecordDTO::from);
    }

    private List<String> findExactHitKeywords(String content) {
        List<String> hits = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return hits;
        }

        List<CensorKeyword> keywords = censorKeywordRepository.findByEnabledTrue();

        for (CensorKeyword keyword : keywords) {
            String word = keyword.getKeyword();

            if (word != null && !word.isBlank() && content.contains(word)) {
                hits.add(word);
            }
        }

        return hits;
    }
}
