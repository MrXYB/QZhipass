package org.microsoft.qintelipass.token;

import java.util.List;
import java.util.Map;

/**
 * 管理员可视化看板数据。
 */
public record DashboardData(
        long activeUsers,
        long overQuotaUsers,
        long quota,
        List<String> dates,
        Map<String, List<Long>> models,
        List<Long> totals
) {
}
