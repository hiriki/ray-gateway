package cn.ray.gateway.common.config;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ray
 * @date 2023/11/20 08:51
 * @description 动态服务缓存配置管理类
 */
public class DynamicConfigManager {

    /**
     * 服务定义集合缓存：uniqueId代表服务的唯一标识
     */
    private ConcurrentHashMap<String /* uniqueId */ , ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 服务实例集合缓存：uniqueId与多个服务实例对应
     */
    private ConcurrentHashMap<String /* uniqueId */ , Set<ServiceInstance>>  serviceInstanceMap = new ConcurrentHashMap<>();

    /**
     * 规则集合
     */
    private ConcurrentHashMap<String /* ruleId */ , Rule>  ruleMap = new ConcurrentHashMap<>();

    private DynamicConfigManager() {
    }

    private static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }

    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /***************** 	对服务定义缓存进行操作的系列方法 	***************/

    public void putServiceDefinition(String uniqueId, ServiceDefinition serviceDefinition) {
        serviceDefinitionMap.put(uniqueId, serviceDefinition);;
    }

    public ServiceDefinition getServiceDefinition(String uniqueId) {
        return serviceDefinitionMap.get(uniqueId);
    }

    public void removeServiceDefinition(String uniqueId) {
        serviceDefinitionMap.remove(uniqueId);
    }

    public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
        return serviceDefinitionMap;
    }

    /***************** 	对服务实例缓存进行操作的系列方法 	***************/
    public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        serviceInstances.add(serviceInstance);
    }

    public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
        serviceInstanceMap.put(uniqueId, serviceInstanceSet);
    }

    public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> iterator = serviceInstances.iterator();
        while(iterator.hasNext()) {
            ServiceInstance instance = iterator.next();
            if (instance.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                iterator.remove();
                break;
            }
        }
        serviceInstances.add(serviceInstance);
    }

    public void removeServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> iterator = serviceInstances.iterator();
        while(iterator.hasNext()) {
            ServiceInstance instance = iterator.next();
            if (instance.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                iterator.remove();
                break;
            }
        }
    }

    public void removeServiceInstances(String uniqueId) {
        serviceInstanceMap.remove(uniqueId);
    }

    public ConcurrentHashMap<String, Set<ServiceInstance>> getServiceInstanceMap() {
        return serviceInstanceMap;
    }

    /***************** 	对规则缓存进行操作的系列方法 	***************/

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    public Rule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    public ConcurrentHashMap<String, Rule> getRuleMap() {
        return ruleMap;
    }
}
