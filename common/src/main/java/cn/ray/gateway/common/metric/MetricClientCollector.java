package cn.ray.gateway.common.metric;

/**
 * @author Ray
 * @date 2024/2/8 21:35
 * @description 指标收集器接口
 */
public interface MetricClientCollector {

    void start();

    void shutdown();

}
