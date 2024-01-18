package cn.ray.gateway.common.constants;

/**
 * @author Ray
 * @date 2023/11/30 19:58
 * @description 所有过滤器常量配置定义
 */
public interface ProcessorFilterConstants {

    String TIMEOUT_PRE_FILTER_ID = "timeoutPreFilter";
    String TIMEOUT_PRE_FILTER_NAME = "超时前置过滤器";
    int TIMEOUT_PRE_FILTER_ORDER = 2100;

    String LOAD_BALANCE_PRE_FILTER_ID = "loadBalancePreFilter";
    String LOAD_BALANCE_PRE_FILTER_NAME = "负载均衡前置过滤器";
    int LOAD_BALANCE_PRE_FILTER_ORDER = 2000;
}
