package cn.ray.gateway.console.service.impl;

import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.common.config.ServiceInvoker;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.common.utils.Pair;
import cn.ray.gateway.console.service.IServiceDefinitionService;
import cn.ray.gateway.discovery.api.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date 2024/2/20 11:33
 * @description
 */
@Service
public class ServiceDefinitionService implements IServiceDefinitionService {

    @Autowired
    private RegistryService registryService;

    @Override
    public List<ServiceDefinition> getServiceDefinitionList(String namespace, String env) throws Exception {
        String path = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.SERVICE_PREFIX;
        List<Pair<String, String>> list = registryService.getListByPrefixPath(path);
        List<ServiceDefinition> serviceDefinitions = new ArrayList<>();
        for(Pair<String, String> pair : list) {
            String p = pair.getObject1();
            if (p.equals(path)) {
                continue;
            }
            String json = pair.getObject2();
            ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
            serviceDefinitions.add(sd);
        }
        return serviceDefinitions;
    }

    @Override
    public void updatePatternPathByUniqueId(String namespace, String env, String uniqueId, String patternPath) throws Exception {
        updateServiceDefinitionByUniqueId(namespace, env, uniqueId, false);
    }

    @Override
    public void updateEnableByUniqueId(String namespace, String env, String uniqueId, boolean enable) throws Exception {
        updateServiceDefinitionByUniqueId(namespace, env, uniqueId, enable);
    }

    @Override
    public void updateServiceDefinitionByUniqueId(String namespace, String env, String uniqueId, Object param) throws Exception {
        String path = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.SERVICE_PREFIX
                + RegistryService.PATH
                + uniqueId;
        Pair<String, String> pair = registryService.getByKey(path);
        String key = pair.getObject1();
        String json = pair.getObject2();
        ServiceDefinition serviceDefinition = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
        //	update:  patternPath & enable
        if(param instanceof String) {
            String patternPath = (String) param;
            serviceDefinition.setPatternPath(patternPath);
        }
        if(param instanceof Boolean) {
            boolean enable = (boolean)param;
            serviceDefinition.setEnable(enable);
        }
        String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
        registryService.registerPermanentNode(key, value);
    }

    @Override
    public List<ServiceInvoker> getServiceInvokerByUniqueId(String namespace, String env, String uniqueId) throws Exception {
        String path = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.SERVICE_PREFIX
                + RegistryService.PATH
                + uniqueId;
        Pair<String, String> pair = registryService.getByKey(path);
        List<ServiceInvoker> invokerList = new ArrayList<>();
        String json = pair.getObject2();
        ServiceDefinition serviceDefinition = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
        Map<String, ServiceInvoker> map = serviceDefinition.getInvokerMap();
        Iterator<ServiceInvoker> it = map.values().iterator();
        while(it.hasNext()) {
            invokerList.add(it.next());
        }
        return invokerList;
    }

    @Override
    public void serviceInvokerBindingRuleId(String namespace, String env, String uniqueId, String invokerPath, String ruleId) throws Exception {
        String path = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.SERVICE_PREFIX
                + RegistryService.PATH
                + uniqueId;
        Pair<String, String> pair = registryService.getByKey(path);
        String key = pair.getObject1();
        String json = pair.getObject2();
        ServiceDefinition serviceDefinition = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
        Map<String, ServiceInvoker> map = serviceDefinition.getInvokerMap();
        for(Map.Entry<String, ServiceInvoker> me : map.entrySet()) {
            String pathKey = me.getKey();
            ServiceInvoker invokerValue = me.getValue();
            if(pathKey.equals(invokerPath)) {
                //	绑定ruleId
                invokerValue.setRuleId(ruleId);
            }
        }
        String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
        registryService.registerPermanentNode(key, value);
    }
}
