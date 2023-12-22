package cn.ray.gateway.core.discovery;

import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.common.utils.ServiceLoader;
import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.discovery.api.Notify;
import cn.ray.gateway.discovery.api.Registry;
import cn.ray.gateway.discovery.api.RegistryService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ray
 * @date 2023/12/22 16:58
 * @description 网关服务的注册中心管理类
 */
@Slf4j
public class RegistryManager {

    private RegistryManager() {}

    private static class SingletonHolder {
        private static final RegistryManager INSTANCE = new RegistryManager();
    }

    public static RegistryManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private GatewayConfig gatewayConfig;

    private RegistryService registryService;

    // 注册中心相关常量
    protected static String superPath;

    protected static String servicesPath;

    protected static String instancesPath;

    protected static String rulesPath;

    private static String gatewaysPath;

    public void initialized(GatewayConfig gatewayConfig) throws Exception {
        this.gatewayConfig = gatewayConfig;

        //	1. 路径的设置
        superPath = Registry.PATH + gatewayConfig.getNamespace() + BasicConstants.BAR_SEPARATOR ;
        servicesPath = superPath + Registry.SERVICE_PREFIX;
        instancesPath = superPath + Registry.INSTANCE_PREFIX;
        rulesPath = superPath + Registry.RULE_PREFIX;
        gatewaysPath = superPath + Registry.GATEWAY_PREFIX;

        //	2. 初始化加载注册中心对象
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        for(RegistryService registryService : serviceLoader) {
            registryService.initialized(gatewayConfig.getRegistryAddress());
            this.registryService = registryService;
        }

        //	3. 注册监听
        this.registryService.addWatcher(superPath, new ServiceListener());

        //	4.订阅服务
        subscribeService();

        //	5.注册自身网关服务
        RegistryServer registryServer = new RegistryServer(registryService);
        registryServer.registerSelf();
    }

    /**
     * 订阅服务的方法：拉取Etcd注册中心的所有需要使用的元数据信息，解析并放置到缓存中
     */
    private void subscribeService() {

    }

    class ServiceListener implements Notify {
        @Override
        public void put(String key, String value) throws Exception {

        }

        @Override
        public void delete(String key) throws Exception {

        }
    }

    /**
     * 网关注册服务
     */
    class RegistryServer {

        private RegistryService registryService;

        private String selfPath;

        public RegistryServer(RegistryService registryService) throws Exception {
            this.registryService = registryService;
            this.registryService.registerPathIfNotExists(superPath, "", true);
            this.registryService.registerPathIfNotExists(gatewaysPath, "", true);
            this.selfPath = gatewaysPath + Registry.PATH + gatewayConfig.getGatewayId();
        }

        public void registerSelf() throws Exception {
            String rapidConfigJson = FastJsonConvertUtil.convertObjectToJSON(gatewayConfig);
            this.registryService.registerPathIfNotExists(selfPath, rapidConfigJson, false);
        }
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

    public static String getGatewaysPath() {
        return gatewaysPath;
    }
}
