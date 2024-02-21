package cn.ray.gateway.console.dto;

import cn.ray.gateway.common.config.Rule;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ray
 * @date 2024/2/12 22:00
 * @description
 */
public class RuleDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 环境
     */
    private String env;

    /**
     * 规则ID
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 服务唯一ID
     */
    private String uniqueId;

    /**
     * 下游转发协议
     */
    private String protocol;

    /**
     * 规则排序
     */
    private Integer order;

    /**
     * 规则集合
     */
    private Set<Rule.FilterConfig> filterConfigs = new HashSet<>();

    public RuleDTO() {}

    public RuleDTO(String namespace, String id, String name, String uniqueId, String protocol, Integer order,
                   Set<Rule.FilterConfig> filterConfigs) {
        super();
        this.namespace = namespace;
        this.id = id;
        this.name = name;
        this.uniqueId = uniqueId;
        this.protocol = protocol;
        this.order = order;
        this.filterConfigs = filterConfigs;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Set<Rule.FilterConfig> getFilterConfigs() {
        return filterConfigs;
    }

    public void setFilterConfigs(Set<Rule.FilterConfig> filterConfigs) {
        this.filterConfigs = filterConfigs;
    }
}
