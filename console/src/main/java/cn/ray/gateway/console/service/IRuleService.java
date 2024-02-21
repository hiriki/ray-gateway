package cn.ray.gateway.console.service;

import cn.ray.gateway.common.config.Rule;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/12 22:15
 * @description
 */

public interface IRuleService {

    /**
     * 获取命名空间内所属环境的所有规则列表
     * @param namespace 命名空间
     * @param env 环境
     * @return
     * @throws Exception
     */
    List<Rule> getRuleList(String namespace, String env) throws Exception;

    /**
     * 添加指定规则
     * @param namespace 命名空间
     * @param env 环境
     * @param rule
     * @throws Exception
     */
    void addRule(String namespace, String env, Rule rule) throws Exception;

    /**
     * 修改指定规则
     * @param namespace 命名空间
     * @param env 环境
     * @param rule
     * @throws Exception
     */
    void updateRule(String namespace, String env, Rule rule) throws Exception;

    /**
     * 删除指定规则
     * @param namespace 命名空间
     * @param env 环境
     * @param ruleId
     */
    void deleteRule(String namespace, String env, String ruleId);
}
