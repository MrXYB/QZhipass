package org.microsoft.qintelipass.ai.token;

/**
 * 某用户当日的 Token 限额与使用状态。
 */
public record UserTokenStatus(
        Long userId,
        long quota,
        long used,
        long remaining,
        boolean overQuota,
        String department,
        String userName
) {
    /**
     * 兼容旧构造器（无部门和姓名）。
     */
    public UserTokenStatus(Long userId, long quota, long used, long remaining, boolean overQuota) {
        this(userId, quota, used, remaining, overQuota, null, null);
    }
}
