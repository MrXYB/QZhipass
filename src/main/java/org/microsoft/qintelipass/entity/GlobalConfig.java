package org.microsoft.qintelipass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Token 模块的键值配置。
 *
 * <p>用于保存全局 Token 配额和按用户设置的独立配额。</p>
 */
@Entity
@Table(name = "global_config")
public class GlobalConfig {

    @Id
    @Column(name = "config_key", nullable = false, length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, length = 500)
    private String value;

    protected GlobalConfig() {
    }

    public GlobalConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
