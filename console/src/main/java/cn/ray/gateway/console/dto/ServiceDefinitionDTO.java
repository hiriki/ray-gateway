package cn.ray.gateway.console.dto;

/**
 * @author Ray
 * @date 2024/2/12 22:08
 * @description
 */
public class ServiceDefinitionDTO {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 环境
     */
    private String env;

    /**
     * 服务唯一ID
     */
    private String uniqueId;

    /**
     * 访问真实ANT表达式匹配
     */
    private String patternPath;

    /**
     * 启用禁用服务
     */
    private boolean enable;

    public ServiceDefinitionDTO() {}

    public ServiceDefinitionDTO(String namespace, String env, String uniqueId, String patternPath, boolean enable) {
        super();
        this.namespace = namespace;
        this.env = env;
        this.uniqueId = uniqueId;
        this.patternPath = patternPath;
        this.enable = enable;
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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getPatternPath() {
        return patternPath;
    }

    public void setPatternPath(String patternPath) {
        this.patternPath = patternPath;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
