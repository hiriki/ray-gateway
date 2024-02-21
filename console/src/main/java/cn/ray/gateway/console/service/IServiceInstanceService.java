package cn.ray.gateway.console.service;

import cn.ray.gateway.common.config.ServiceInstance;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/21 09:11
 * @description
 */
public interface IServiceInstanceService {

    /**
     * 根据服务唯一ID获取实例列表
     * @param namespace
     * @param env
     * @param uniqueId
     * @return
     * @throws Exception
     */
    List<ServiceInstance> getServiceInstanceList(String namespace, String env, String uniqueId) throws Exception;

    /**
     * 根据服务唯一ID以及IP端口(serviceInstanceId)修改服务实例是否禁用
     * @param namespace
     * @param env
     * @param uniqueId
     * @param serviceInstanceId
     * @param enable
     * @throws Exception
     */
    void updateEnable(String namespace, String env, String uniqueId, String serviceInstanceId, boolean enable) throws Exception;

    /**
     * 根据服务唯一ID以及IP端口(serviceInstanceId)修改服务实例标签
     * @param namespace
     * @param env
     * @param uniqueId
     * @param serviceInstanceId
     * @param tags
     * @throws Exception
     */
    void updateTags(String namespace, String env, String uniqueId, String serviceInstanceId, String tags) throws Exception;

    /**
     * 根据服务唯一ID以及IP端口(serviceInstanceId)修改服务实例权重
     * @param namespace
     * @param env
     * @param uniqueId
     * @param serviceInstanceId
     * @param weight
     * @throws Exception
     */
    void updateWeight(String namespace, String env, String uniqueId, String serviceInstanceId, int weight) throws Exception;

    /**
     * 根据服务唯一ID以及IP端口(serviceInstanceId)修改服务实例 enable tags weight
     * @param namespace
     * @param env
     * @param uniqueId
     * @param serviceInstanceId
     * @param param
     * @throws Exception
     */
    void updateServiceInstance(String namespace, String env, String uniqueId, String serviceInstanceId, Object param) throws Exception;
}
