package cn.ray.gateway.core.balance;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.core.context.GatewayContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Ray
 * @date 2024/1/17 15:58
 * @description 加权随机负载均衡
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * 随机负载均衡方法
     * @param context
     * @param instances
     * @return
     * @see cn.ray.gateway.core.balance.AbstractLoadBalance#doSelect(cn.ray.gateway.core.context.GatewayContext, java.util.List)
     */
    @Override
    protected ServiceInstance doSelect(GatewayContext context, List<ServiceInstance> instances) {
        int length = instances.size();
        //	总权重
        int totalWeight = 0;
        //	是否每个实例的权重都相同
        boolean sameWeight = true;
        //	0 == > 100    1 ==> 80    2 ==> 40
        for (int i = 0; i < length; i++) {
            //	获取真实权重
            int weight = getWeight(instances.get(i));
            //	计算总的权重
            totalWeight += weight;
            //	前后比较权重：有权重不一样的实例, 走权重不一致的逻辑
            if (sameWeight && i > 0 && weight != getWeight(instances.get(i - 1))) {
                sameWeight = false;
            }
        }
        //	权重不一致的逻辑 : totalWeight = 220
        if (totalWeight > 0 && !sameWeight) {
            // 	根据总权重随机出一个偏移量 offset = 122
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            // 	根据偏移量找到靠近偏移量范围的实例
            for (ServiceInstance instance : instances) {
                //	0 == > 100    1 ==> 80    2 ==> 40
                // [0,99] [101,179] [180-229]
                // 122 - 100 = 22
                // 22 - 80 return
                offset = offset - getWeight(instance);
                if (offset < 0) {
                    return instance;
                }
            }
        }
        // 	如果所有实例权重一致, 使用随机的一个实例即可
        return instances.get(ThreadLocalRandom.current().nextInt(length));
    }
}
