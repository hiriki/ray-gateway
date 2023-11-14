package cn.ray.gateway.common.config;

/**
 * @author Ray
 * @date 2023/11/13 22:21
 * @description 服务调用的接口模型描述
 */
public interface ServiceInvoker {

    /**
     * 获取服务调用的全路径
     * @return
     */
    String getInvokerPath();

    /**
     * 设置服务调用的全路径
     * @param invokerPath
     */
    void setInvokerPath(String invokerPath);

    /**
     * 获取指定服务调用绑定的唯一规则
     * @return
     */
    String getRuleId();

    /**
     * 设置指定服务调用绑定的唯一规则
     * @param ruleId
     */
    void setRuleId(String ruleId);

    /**
     * 获取该服务调用(方法)的超时时间
     * @return
     */
    int getTimeout();

    /**
     * 设置该服务调用(方法)的超时时间
     * @param timeout
     */
    void setTimeout(int timeout);
}
