package cn.ray.gateway.core.balance;

import cn.ray.gateway.common.enums.LoadBalanceStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ray
 * @date 2024/1/17 15:57
 * @description
 */
public class LoadBalanceFactory {

    private final Map<LoadBalanceStrategy, LoadBalance> loadBalanceMap = new HashMap<>();

    private static final LoadBalanceFactory INSTANCE = new LoadBalanceFactory();

    private LoadBalanceFactory() {
        loadBalanceMap.put(LoadBalanceStrategy.RANDOM, new RandomLoadBalance());
        loadBalanceMap.put(LoadBalanceStrategy.ROUND_ROBIN, new RoundRobinLoadBalance());
    }

    public static LoadBalance getLoadBalance(LoadBalanceStrategy loadBalance) {
        return INSTANCE.loadBalanceMap.get(loadBalance);
    }
}
