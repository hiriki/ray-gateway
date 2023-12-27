package cn.ray.gateway.core.discovery;

import cn.ray.gateway.common.config.*;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.constants.GatewayProtocol;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.common.utils.Pair;
import cn.ray.gateway.common.utils.ServiceLoader;
import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.discovery.api.Notify;
import cn.ray.gateway.discovery.api.Registry;
import cn.ray.gateway.discovery.api.RegistryService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;

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
    private static String superPath;

    private static String servicesPath;

    private static String instancesPath;

    private static String rulesPath;

    private static String gatewaysPath;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

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
        this.registryService.addWatchers(superPath, new ServiceListener());

        // 这里存在极限情况: 注册监听的同时服务发生变化并且服务已经开始订阅, put delete 相关操作未执行, 可能缓存到脏数据
        // 使用 CountDownLatch 阻塞监听到变化后的 put 、delete 操作, 直到订阅完成

        //	4.订阅服务
        subscribeService();

        //	5.注册自身网关服务
        RegistryServer registryServer = new RegistryServer(registryService);
        registryServer.registerSelf();
    }

    /**
     * 订阅服务的方法：拉取Etcd注册中心的所有需要使用的元数据信息，解析并放置到缓存 DynamicConfigManager 中
     * 		/ray-gateway-dev
     * 			/services
     * 				/hello:1.0.0
     * 				/say:1.0.0
     * 			/instances
     * 				/hello:1.0.0/192.168.11.100:1234
     * 				/hello:1.0.0/192.168.11.101:4321
     */
    private void subscribeService() {
        log.info("#RegistryManager#subscribeService  ------------ 	服务订阅开始 	---------------");

        try {
            //	1. 加载服务定义和服务实例的集合: 获取 servicesPath = /ray-gateway-dev/services 下面所有的列表
            List<Pair<String, String>> definitionList = this.registryService.getListByPrefixPath(servicesPath);

            for (Pair<String, String> definition : definitionList) {
                String definitionPath = definition.getObject1();
                String definitionJson = definition.getObject2();

                // 排除服务定义的根目录 servicesPath = /ray-gateway-dev/services
                if (definitionPath.equals(servicesPath)) {
                    continue;
                }

                //	1.1 加载服务定义信息:
                String uniqueId = definitionPath.substring(servicesPath.length() + 1);
                ServiceDefinition serviceDefinition = parseServiceDefinition(definitionJson);
                DynamicConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
                log.info("#RegistryManager#subscribeService 1.1 加载服务定义信息 uniqueId : {}, serviceDefinition : {}",
                        uniqueId,
                        FastJsonConvertUtil.convertObjectToJSON(serviceDefinition));

                //	1.2 加载服务实例集合:
                //	拼接当前服务定义的服务实例前缀路径
                String serviceInstancePrefix = instancesPath + Registry.PATH + uniqueId;
                List<Pair<String, String>> instanceList = this.registryService.getListByPrefixPath(serviceInstancePrefix);
                Set<ServiceInstance> serviceInstanceSet = new HashSet<>();
                for(Pair<String, String> instance : instanceList) {
                    String instancePath = instance.getObject1();
                    String instanceJson = instance.getObject2();

                    // 排除当前服务实例的根目录 instancePath = /ray-gateway-dev/instances/hello:1.0.0
                    if (instancePath.equals(serviceInstancePrefix)) {
                        continue;
                    }

                    ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(instanceJson, ServiceInstance.class);
                    serviceInstanceSet.add(serviceInstance);
                }

                DynamicConfigManager.getInstance().addServiceInstance(uniqueId, serviceInstanceSet);
                log.info("#RegistryManager#subscribeService 1.2 加载服务实例集合 uniqueId : {}, serviceInstanceSet : {}",
                        uniqueId,
                        FastJsonConvertUtil.convertObjectToJSON(serviceInstanceSet));
            }

            //	2. 加载规则集合:
            List<Pair<String, String>> ruleList = this.registryService.getListByPrefixPath(rulesPath);

            for (Pair<String, String> r : ruleList) {
                String rulePath = r.getObject1();
                String ruleJson = r.getObject2();

                // 排除规则的根目录 rulesPath = /ray-gateway-dev/rules
                if (rulePath.equals(rulesPath)) {
                    continue;
                }

                Rule rule = FastJsonConvertUtil.convertJSONToObject(ruleJson, Rule.class);
                DynamicConfigManager.getInstance().putRule(rule.getId(), rule);
                log.info("#RegistryManager#subscribeService 2 加载规则信息 ruleId : {}, rule : {}",
                        rule.getId(),
                        FastJsonConvertUtil.convertObjectToJSON(rule));
            }

        } catch (Exception e) {
            log.error("#RegistryManager#subscribeService 服务订阅失败 ", e);
        } finally {
            countDownLatch.countDown();
            log.info("#RegistryManager#subscribeService  ------------ 	服务订阅结束 	---------------");
        }
    }

    /**
     * 把从注册中心拉取过来的 json 字符串转换成指定的 ServiceDefinition
     * @param definitionJson
     * @return ServiceDefinition
     */
    @SuppressWarnings("unchecked")
    private ServiceDefinition parseServiceDefinition(String definitionJson) {
        Map<String, Object> jsonMap = FastJsonConvertUtil.convertJSONToObject(definitionJson, Map.class);

        ServiceDefinition serviceDefinition = new ServiceDefinition();

        //	填充serviceDefinition
        serviceDefinition.setUniqueId((String)jsonMap.get("uniqueId"));
        serviceDefinition.setServiceId((String)jsonMap.get("serviceId"));
        serviceDefinition.setProtocol((String)jsonMap.get("protocol"));
        serviceDefinition.setPatternPath((String)jsonMap.get("patternPath"));
        serviceDefinition.setVersion((String)jsonMap.get("version"));
        serviceDefinition.setEnable((boolean)jsonMap.get("enable"));
        serviceDefinition.setEnvType((String)jsonMap.get("envType"));

        Map<String, ServiceInvoker> invokerMap = new HashMap<>();
        JSONObject jsonInvokerMap = (JSONObject)jsonMap.get("invokerMap");

        switch (serviceDefinition.getProtocol()) {
            case GatewayProtocol.HTTP:
                Map<String, Object> httpInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
                for(Map.Entry<String, Object> entry : httpInvokerMap.entrySet()) {
                    String invokerPath = entry.getKey();
                    JSONObject jsonInvoker = (JSONObject) entry.getValue();
                    HttpServiceInvoker httpServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, HttpServiceInvoker.class);
                    invokerMap.put(invokerPath, httpServiceInvoker);
                }
                break;
            case GatewayProtocol.DUBBO:
                Map<String, Object> dubboInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
                for(Map.Entry<String, Object> entry : dubboInvokerMap.entrySet()) {
                    String invokerPath = entry.getKey();
                    JSONObject jsonInvoker = (JSONObject) entry.getValue();
                    DubboServiceInvoker dubboServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, DubboServiceInvoker.class);
                    invokerMap.put(invokerPath, dubboServiceInvoker);
                }
                break;
            default:
                break;
        }

        serviceDefinition.setInvokerMap(invokerMap);

        return serviceDefinition;
    }

    class ServiceListener implements Notify {
        @Override
        public void put(String key, String value) throws Exception {
            countDownLatch.await();
        }

        @Override
        public void delete(String key) throws Exception {
            countDownLatch.await();
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
