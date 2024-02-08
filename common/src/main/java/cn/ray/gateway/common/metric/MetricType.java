package cn.ray.gateway.common.metric;

/**
 * @author Ray
 * @date 2024/2/8 21:25
 * @description 指标类型常量类
 */
public interface MetricType {

    /**
     * 标签固有属性
     */
    String KEY = "type";

    String STATISTICS = "statistics";

    String LOAD = "load";
}
