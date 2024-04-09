package cn.ray.gateway.client.core;

import cn.ray.gateway.client.core.config.GatewayClientProperties;
import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.common.utils.ServiceLoader;
import cn.ray.gateway.discovery.api.Registry;
import cn.ray.gateway.discovery.api.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ray
 * @date 2023/12/7 16:35
 * @description 抽象注册管理器, 提供构建顶级目录以及注册服务定义实例方法等通用方法
 */
@Slf4j
public abstract class AbstractClientRegistryManager {

    public static final String PROPERTIES_PATH = "gateway.properties";

    public static final String REGISTRY_ADDRESS_KEY = "registryAddress";

    public static final String NAMESPACE_KEY = "namespace";

    public static final String ENV_KEY = "env";

    public static final String TAGS_KEY = "tags";

    protected volatile boolean whetherStart = false;

    protected static String registryAddress;

    protected static String namespace;

    protected static String env;

    protected static String tags;

    public static Properties properties = new Properties();

    // 注册中心相关常量
    protected static String superPath;

    protected static String servicesPath;

    protected static String instancesPath;

    protected static String rulesPath;

    private RegistryService registryService;

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
                tags = properties.getProperty(TAGS_KEY);

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
    protected AbstractClientRegistryManager(GatewayClientProperties gatewayClientProperties) throws Exception {
        //	1. 初始化加载配置信息
        if (gatewayClientProperties.getRegistryAddress() != null) {
            registryAddress = gatewayClientProperties.getRegistryAddress();
            namespace = gatewayClientProperties.getNamespace();
            if (StringUtils.isBlank(namespace)) {
                namespace = GatewayClientProperties.PROPERTIES_PREFIX;
            }
            env = gatewayClientProperties.getEnv();
            tags = gatewayClientProperties.getTags();
        }

        //  2. 初始化加载注册中心对象
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        for(RegistryService registryService : serviceLoader) {
            registryService.initialized(gatewayClientProperties.getRegistryAddress());
            this.registryService = registryService;
        }

        //	3. 注册构建顶级目录结构
        generateStructPath(Registry.PATH + namespace + BasicConstants.BAR_SEPARATOR + env);
    }

    /**
     * 注册顶级结构目录路径，只需要构建一次即可
     *
     * @param path
     * @throws Exception
     */
    private void generateStructPath(String path) throws Exception {
        /**
         * 	/ray-gateway-env
         * 		/services
         * 			/serviceA:1.0.0  ==> ServiceDefinition
         * 			/serviceA:2.0.0
         * 			/serviceB:1.0.0
         * 		/instances
         * 			/serviceA:1.0.0/192.168.11.100:port	 ==> ServiceInstance
         * 			/serviceA:1.0.0/192.168.11.101:port
         * 			/serviceB:1.0.0/192.168.11.102:port
         * 			/serviceA:2.0.0/192.168.11.103:port
         * 		/rules
         * 			/ruleId1	==>	Rule
         * 			/ruleId2
         * 		/gateway
         */
        superPath = path;
        servicesPath = superPath + Registry.SERVICE_PREFIX;
        instancesPath = superPath + Registry.INSTANCE_PREFIX;
        rulesPath = superPath + Registry.RULE_PREFIX;

        registryService.registerPathIfNotExists(superPath, "", true);
        registryService.registerPathIfNotExists(servicesPath, "", true);
        registryService.registerPathIfNotExists(instancesPath, "", true);
        registryService.registerPathIfNotExists(rulesPath, "", true);
    }

    /**
     * 注册服务定义
     * @param serviceDefinition ServiceDefinition
     * @throws Exception
     */
    protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {
        String key = servicesPath
                + Registry.PATH
                + serviceDefinition.getUniqueId();

        if (!registryService.isExist(key)) {
            String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
            registryService.registerPathIfNotExists(key, value, true);
        }
    }

    /**
     * 注册服务实例
     * @param serviceInstance ServiceInstance
     * @throws Exception
     */
    protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {
        String key = instancesPath
                + Registry.PATH
                + serviceInstance.getUniqueId()
                + Registry.PATH
                + serviceInstance.getServiceInstanceId();

        if (!registryService.isExist(key)) {
            String value = FastJsonConvertUtil.convertObjectToJSON(serviceInstance);
            registryService.registerPathIfNotExists(key, value, false);
        }
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

    public static String getSuperPath() {
        return superPath;
    }

    public static String getServicesPath() {
        return servicesPath;
    }

    public static String getInstancesPath() {
        return instancesPath;
    }

    public static String getRulesPath() {
        return rulesPath;
    }

    public static String getTags() {
        return tags;
    }
}
