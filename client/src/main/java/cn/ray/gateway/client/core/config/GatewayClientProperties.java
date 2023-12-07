package cn.ray.gateway.client.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Ray
 * @date 2023/12/7 02:06
 * @description 应用配置
 */
@Data
@ConfigurationProperties(prefix = GatewayClientProperties.PROPERTIES_PREFIX)
public class GatewayClientProperties {

    public static final String PROPERTIES_PREFIX = "ray-gateway";

    /**
     * 	etcd注册中心地址
     */
    private String registryAddress;

    /**
     * 	etcd注册命名空间
     */
    private String namespace = PROPERTIES_PREFIX;

    /**
     * 	环境属性
     */
    private String env = "dev";
}
