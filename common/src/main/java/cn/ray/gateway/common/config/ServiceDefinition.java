package cn.ray.gateway.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ray
 * @date 2023/11/13 22:15
 * @description 资源服务定义类：无论下游是什么样的服务都需要进行注册
 */
@Data
public class ServiceDefinition implements Serializable {

    private static final long serialVersionUID = -8263365765897285189L;

    /**
     * 唯一ID: serviceId:version
     */
    private String uniqueId;

    /**
     * 服务唯一ID
     */
    private String serviceId;

    /**
     * 服务版本号
     */
    private String version;

    /**
     * 服务调用协议: http、dubbo
     */
    private String protocol;

    /**
     * 路径匹配规则: 访问真实ANT表达式：定义具体的服务路径的匹配规则:
     * 比如用户服务: /user/*
     */
    private String patternPath;

    /**
     * 环境类型
     */
    private String envType;

    /**
     * 服务是否启用
     */
    private boolean enable = true;

    /**
     * 服务列表信息
     */
    private Map<String /* invokerPath */, ServiceInvoker> invokerMap;

    public ServiceDefinition() {
    }

    public ServiceDefinition(String uniqueId, String serviceId, String version,
                             String protocol, String patternPath, String envType,
                             boolean enable, Map<String, ServiceInvoker> invokerMap) {
        this.uniqueId = uniqueId;
        this.serviceId = serviceId;
        this.version = version;
        this.protocol = protocol;
        this.patternPath = patternPath;
        this.envType = envType;
        this.enable = enable;
        this.invokerMap = invokerMap;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(this == null || getClass() != o.getClass()) return false;
        ServiceDefinition serviceDefinition = (ServiceDefinition)o;
        return Objects.equals(this.uniqueId, serviceDefinition.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
