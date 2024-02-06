package cn.ray.gateway.common.constants;

/**
 * @author Ray
 * @date 2023/11/30 19:58
 * @description 所有过滤器常量配置定义
 */
public interface ProcessorFilterConstants {

    String LOAD_BALANCE_PRE_FILTER_ID = "loadBalancePreFilter";
    String LOAD_BALANCE_PRE_FILTER_NAME = "负载均衡前置过滤器";
    int LOAD_BALANCE_PRE_FILTER_ORDER = 2000;

    String TIMEOUT_PRE_FILTER_ID = "timeoutPreFilter";
    String TIMEOUT_PRE_FILTER_NAME = "超时前置过滤器";
    int TIMEOUT_PRE_FILTER_ORDER = 2100;

    String HTTP_ROUTE_FILTER_ID = "httpRouteFilter";
    String HTTP_ROUTE_FILTER_NAME = "HTTP请求转发过滤器";
    int HTTP_ROUTE_FILTER_ORDER = 5000;

    String DUBBO_ROUTE_FILTER_ID = "dubboRouteFilter";
    String DUBBO_ROUTE_FILTER_NAME = "Dubbo请求转发过滤器";
    int DUBBO_ROUTE_FILTER_ORDER = 5000;

    String DEFAULT_ERROR_FILTER_ID = "defaultErrorFilter";
    String DEFAULT_ERROR_FILTER_NAME = "默认异常处理过滤器";
    int DEFAULT_ERROR_FILTER_ORDER = 20000;

    String STATISTICS_POST_FILTER_ID = "statisticsPostFilter";
    String STATISTICS_POST_FILTER_NAME = "统计分析过滤器";
    int STATISTICS_POST_FILTER_ORDER = Integer.MAX_VALUE;
}
