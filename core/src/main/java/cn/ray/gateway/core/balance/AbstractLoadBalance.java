package cn.ray.gateway.core.balance;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.utils.TimeUtil;
import cn.ray.gateway.core.context.AttributeKey;
import cn.ray.gateway.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Ray
 * @date 2023/12/29 01:35
 * @description 负载均衡抽象类, 主要实现预热机制
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public ServiceInstance select(GatewayContext context) {

        // TODO MATCH_INSTANCES：服务实例列表目前还没有填充，需要LoadBalancePreFilter的时候进行获取并设置
        Set<ServiceInstance> matchInstance = context.getAttribute(AttributeKey.MATCH_INSTANCES);

        if(matchInstance == null || matchInstance.isEmpty()) {
            return null;
        }

        List<ServiceInstance> instances = new ArrayList<ServiceInstance>(matchInstance);
        if(instances.size() == 1) {
            return instances.get(0);
        }

        ServiceInstance instance = doSelect(context, instances);
        context.putAttribute(AttributeKey.LOAD_INSTANCE, instance);
        return null;
    }

    /**
     * 由子类实现指定的负载均衡策略
     * @param context
     * @param instances
     * @return
     */
    protected abstract ServiceInstance doSelect(GatewayContext context, List<ServiceInstance> instances);

    /**
     * 获取服务实例权重
     * @param instance
     * @return
     */
    protected static int getWeight(ServiceInstance instance) {
        int weight = instance.getWeight() == null ? LoadBalance.DEFAULT_WEIGHT : instance.getWeight();
        if(weight > 0) {
            //	服务实例注册的时间
            long timestamp = instance.getRegisterTime();
            if(timestamp > 0L) {
                //	服务启动了多久：当前时间 - 注册时间
                int upTime = (int)(TimeUtil.currentTimeMillis() - timestamp);
                //	默认预热时间 5min
                int warmupTime = LoadBalance.DEFAULT_WARMUP_TIME;
                if(upTime > 0 && upTime < warmupTime) {
                    weight = calculateWarmUpWeight(upTime, warmupTime, weight);
                }
            }
        }
        return weight;
    }

    /**
     * 计算服务在预热时间内平衡后的权重
     * @param upTime
     * @param warmupTime
     * @param weight
     * @return
     */
    private static int calculateWarmUpWeight(int upTime, int warmupTime, int weight) {
        // 启动时间 / (预热时间 / 权重)
        // upTime = 2 * 60 * 1000
        // warmupTime = 5 * 60 * 1000
        // weight = 100
        // resWeight = 120/3 = 40
        int resWeight =(int)((float) upTime / ((float) warmupTime / (float) weight));
        return resWeight < 1 ? 1 : (Math.min(resWeight, weight));
    }
}
