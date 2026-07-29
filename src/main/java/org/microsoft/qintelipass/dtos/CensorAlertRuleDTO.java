package org.microsoft.qintelipass.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.microsoft.qintelipass.models.CensorAlertRule;

@Data
public class CensorAlertRuleDTO {
    private Long id;
    private String name;
    private int periodDays;
    private int threshold;
    private Boolean enabled;
    @JsonProperty("isDefault")
    private boolean isDefault;
    private String updatedAt;
    private String createdBy;

    public static CensorAlertRuleDTO from(CensorAlertRule rule) {
        CensorAlertRuleDTO dto = new CensorAlertRuleDTO();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setPeriodDays(rule.getPeriodDays());
        dto.setThreshold(rule.getThreshold());
        dto.setEnabled(rule.isEnabled());
        dto.setDefault(rule.isDefaultRule());
        dto.setCreatedBy(rule.getCreatedBy());
        if (rule.getUpdatedAt() != null) {
            dto.setUpdatedAt(rule.getUpdatedAt().toString());
        }
        return dto;
    }
}
