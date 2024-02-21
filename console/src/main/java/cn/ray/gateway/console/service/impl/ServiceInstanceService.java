package cn.ray.gateway.console.service.impl;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.common.utils.Pair;
import cn.ray.gateway.console.service.IServiceInstanceService;
import cn.ray.gateway.discovery.api.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ray
 * @date 2024/2/21 09:49
 * @description
 */
@Service
public class ServiceInstanceService implements IServiceInstanceService {

    @Autowired
    private RegistryService registryService;

    @Override
    public List<ServiceInstance> getServiceInstanceList(String namespace, String env, String uniqueId) throws Exception {
        /**
         * 		/ray-gateway-env
         * 			/services
         * 				/serviceA:1.0.0		==>	value: ServiceDefinition & AbstractServiceInvoker
         * 				/serviceB:1.0.0
         * 			/instances
         * 			/instances/serviceA:1.0.0/192.168.11.100:port  	==>  value: ServiceInstance1
         * 			/instances/serviceA:1.0.0/192.168.11.101:port		==>  value: ServiceInstance1
         * 			/instances/serviceB:1.0.0
         * 				/192.168.11.103:port  	==>  value: ServiceInstance1
         * 			/routes
         * 				/uuid01	==>	value: Rule1
         * 				/uuid02 ==>	value: Rule2
         */
        String path = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.INSTANCE_PREFIX
                + RegistryService.PATH
                + uniqueId;
        List<Pair<String, String>> list = registryService.getListByPrefixPath(path);

        List<ServiceInstance> serviceInstances = new ArrayList<>();

        for(Pair<String, String> pair : list) {
            String json = pair.getObject2();
            ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(json, ServiceInstance.class);
            serviceInstances.add(serviceInstance);
        }

        return serviceInstances;
    }

    @Override
    public void updateEnable(String namespace, String env, String uniqueId, String serviceInstanceId, boolean enable) throws Exception {
        updateServiceInstance(namespace, env, uniqueId, serviceInstanceId, enable);
    }

    @Override
    public void updateTags(String namespace, String env, String uniqueId, String serviceInstanceId, String tags) throws Exception {
        updateServiceInstance(namespace, env, uniqueId, serviceInstanceId, tags);
    }

    @Override
    public void updateWeight(String namespace, String env, String uniqueId, String serviceInstanceId, int weight) throws Exception {
        updateServiceInstance(namespace, env, uniqueId, serviceInstanceId, weight);
    }

    @Override
    public void updateServiceInstance(String namespace, String env, String uniqueId, String serviceInstanceId, Object param) throws Exception {
        String path = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.INSTANCE_PREFIX
                + RegistryService.PATH
                + uniqueId;
        List<Pair<String, String>> list = registryService.getListByPrefixPath(path);

        for(Pair<String, String> pair : list) {
            String key = pair.getObject1();
            String json = pair.getObject2();
            ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(json, ServiceInstance.class);
            //	更新启用禁用
            if((serviceInstance.getServiceInstanceId()).equals(serviceInstanceId)) {

                //	update:  tags & enable & weight
                if(param instanceof String) {
                    String tags = (String)param;
                    serviceInstance.setTags(tags);
                }
                if(param instanceof Boolean) {
                    boolean enable = (boolean)param;
                    serviceInstance.setEnable(enable);
                }
                if(param instanceof Integer) {
                    int weight = (int)param;
                    serviceInstance.setWeight(weight);
                }

            }
            //	回写数据到ETCD
            String value = FastJsonConvertUtil.convertObjectToJSON(serviceInstance);
            registryService.registerTemporaryNode(key, value);
        }
    }
}
