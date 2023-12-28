package cn.ray.gateway.core.balance;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.core.context.GatewayContext;

/**
 * @author Ray
 * @date 2023/12/29 01:31
 * @description
 */
public interface LoadBalance {

    /**
     * 默认权重
     */
    int DEFAULT_WEIGHT = 100;

    /**
     * 默认预热时间
     */
    int DEFAULT_WARMUP_TIME = 5 * 60 * 1000;

    /**
     * 负载均衡: 从所有实例列表中选择一个实例
     * @param context
     * @return
     */
    ServiceInstance select(GatewayContext context);
}
