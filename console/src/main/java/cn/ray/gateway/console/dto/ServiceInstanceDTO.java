package cn.ray.gateway.console.dto;

/**
 * @author Ray
 * @date 2024/2/12 22:09
 * @description
 */
public class ServiceInstanceDTO {

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
     * 服务实例ID
     */
    private String serviceInstanceId;

    /**
     * 启用禁用服务实例
     */
    private boolean enable = true;

    /**
     * 路由标签
     */
    private String tags;

    /**
     * 权重
     */
    private Integer weight;

    public ServiceInstanceDTO() {}

    public ServiceInstanceDTO(String namespace, String env, String uniqueId,
                              String serviceInstanceId, boolean enable,
                              String tags, Integer weight) {
        super();
        this.namespace = namespace;
        this.env = env;
        this.uniqueId = uniqueId;
        this.serviceInstanceId = serviceInstanceId;
        this.enable = enable;
        this.tags = tags;
        this.weight = weight;
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

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
