package org.microsoft.qintelipass.services;

import org.microsoft.qintelipass.dtos.TokenUsageRankDTO;
import org.microsoft.qintelipass.dtos.UserTokenUsageDTO;
import org.microsoft.qintelipass.entity.Models;
import org.microsoft.qintelipass.entity.User;

import java.util.List;
import java.util.Map;

public interface TokenUsageService {
    boolean recordTokenUsage(User user, Models model, int tokensUsed);
    boolean checkTokenLimit(User user);
    UserTokenUsageDTO getUserTokenUsage(User user);
    List<TokenUsageRankDTO> getDailyTokenRank(int topN);
    long getUserTokenLimit(User user);
    void setUserTokenLimit(User user, long limit);
    String getTodayTotalTokens();
    void increaseDailyTotalTokens(Integer tokens);
    Long getOveruseUsers();
    Long getDailyTokenLimit();
    void setDailyTokenLimit(Long value);
    Map<String, Object> getModelStatisticsForLast7Days();
    void aggregateDailyData();
    Long getActiveUserCount();
    Map<String, Object> getDepartmentStatistics();
    List<Map<String, Object>> getAllUserTokenUsage();
}