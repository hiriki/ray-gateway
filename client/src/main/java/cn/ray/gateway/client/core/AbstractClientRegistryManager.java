package cn.ray.gateway.client.core;

import cn.ray.gateway.client.core.config.GatewayClientProperties;
import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.common.config.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ray
 * @date 2023/12/7 16:35
 * @description 抽象注册管理器
 */
@Slf4j
public abstract class AbstractClientRegistryManager {

    public static final String PROPERTIES_PATH = "gateway.properties";

    public static final String REGISTRY_ADDRESS_KEY = "registryAddress";

    public static final String NAMESPACE_KEY = "namespace";

    public static final String ENV_KEY = "env";

    protected volatile boolean whetherStart = false;

    protected static String registryAddress;

    protected static String namespace;

    protected static String env;

    public static Properties properties = new Properties();

    // TODO: 注册中心相关常量

    //	静态代码块, 类加载时读取 gateway.properties 配置文件
    static {
        InputStream inputStream = null;
        inputStream = AbstractClientRegistryManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
        try {
            if (inputStream != null) {
                properties.load(inputStream);
                registryAddress = properties.getProperty(REGISTRY_ADDRESS_KEY);
                namespace = properties.getProperty(NAMESPACE_KEY);
                env = properties.getProperty(ENV_KEY);

                if (StringUtils.isBlank(registryAddress)) {
                    String errorMessage = "网关注册中心地址不能为空";
                    log.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }

                if (StringUtils.isBlank(namespace)) {
                    namespace = GatewayClientProperties.PROPERTIES_PREFIX;
                }
            }
        } catch (Exception e) {
            log.error("#AbstractClientRegistryManager# InputStream load is error", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("#AbstractClientRegistryManager# InputStream close is error", e);
                }
            }
        }
    }

    /**
     * application.properties/yml 优先级是最高的
     * @param gatewayClientProperties
     */
    protected AbstractClientRegistryManager(GatewayClientProperties gatewayClientProperties) {
        //	1. 初始化加载配置信息
        if (gatewayClientProperties.getRegistryAddress() != null) {
            registryAddress = gatewayClientProperties.getRegistryAddress();
            namespace = gatewayClientProperties.getNamespace();
            if (StringUtils.isBlank(namespace)) {
                namespace = GatewayClientProperties.PROPERTIES_PREFIX;
            }
            env = gatewayClientProperties.getEnv();
        }

        // TODO: 2. 初始化加载注册中心对象
    }

    /**
     * 注册顶级结构目录路径，只需要构建一次即可
     * @param path
     * @throws Exception
     */
    private void generatorStructPath(String path) throws Exception {

    }

    /**
     * 注册服务定义
     * @param serviceDefinition ServiceDefinition
     * @throws Exception
     */
    protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {

    }

    /**
     * 注册服务实例
     * @param serviceInstance ServiceInstance
     * @throws Exception
     */
    protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {

    }

    public static String getRegistryAddress() {
        return registryAddress;
    }

    public static String getNamespace() {
        return namespace;
    }

    public static String getEnv() {
        return env;
    }
}
