package cn.ray.gateway.console.service;

import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.common.config.ServiceInvoker;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/20 10:46
 * @description
 */
public interface IServiceDefinitionService {

    /**
     * 获取命名空间内所属环境的所有服务定义列表
     * @param namespace 命名空间
     * @param env 环境
     * @return
     * @throws Exception
     */
    List<ServiceDefinition> getServiceDefinitionList(String namespace, String env) throws Exception;

    /**
     * 修改匹配路径
     * @param namespace 命名空间
     * @param env 环境
     * @param uniqueId
     * @param patternPath
     * @throws Exception
     */
    void updatePatternPathByUniqueId(String namespace, String env, String uniqueId, String patternPath) throws Exception;

    /**
     * 修改是否禁用
     * @param namespace 命名空间
     * @param env 环境
     * @param uniqueId
     * @param enable
     * @throws Exception
     */
    void updateEnableByUniqueId(String namespace, String env, String uniqueId, boolean enable) throws Exception;

    /**
     * 根据服务唯一ID更新patternPath enable
     * @param namespace 命名空间
     * @param env 环境
     * @param uniqueId
     * @param param
     * @throws Exception
     */
    void updateServiceDefinitionByUniqueId(String namespace, String env, String uniqueId, Object param) throws Exception;

    /**
     * 根据服务唯一ID获取指定的服务下的调用方法列表
     * @param namespace 命名空间
     * @param env 环境
     * @param uniqueId
     * @return
     * @throws Exception
     */
    List<ServiceInvoker> getServiceInvokerByUniqueId(String namespace, String env, String uniqueId) throws Exception;

    /**
     * 为ServiceInvoker绑定一个规则ID
     * @param namespace 命名空间
     * @param env 环境
     * @param uniqueId
     * @param invokerPath
     * @param ruleId
     * @throws Exception
     */
    void serviceInvokerBindingRuleId(String namespace, String env, String uniqueId, String invokerPath, String ruleId) throws Exception;

}
