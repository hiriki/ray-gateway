package cn.ray.gateway.core.netty.processor.filter;

import cn.ray.gateway.core.context.Context;

import java.util.List;

/**
 * @author Ray
 * @date 2023/11/24 09:06
 * @description 过滤器工厂接口
 */
public interface ProcessorFilterFactory {

    /**
     * 根据过滤器类型，添加一组过滤器，用于构建过滤器链
     * @param filterType
     * @param filters
     * @throws Exception
     */
    void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception;

    /**
     * 正常情况下执行过滤器链
     * @param context
     * @throws Exception
     */
    void doFilterChain(Context context) throws Exception;

    /**
     * 错误、异常情况下执行过滤器链
     * @param context
     * @throws Exception
     */
    void doErrorFilterChain(Context context) throws Exception;

    /**
     * 获取指定类型的过滤器
     * @param t
     * @return
     * @param <T>
     * @throws Exception
     */
    <T> T getFilter(Class<T> t) throws Exception;

    /**
     * 获取指定ID的过滤器
     * @param filterId
     * @return
     * @param <T>
     * @throws Exception
     */
    <T> T getFilter(String filterId) throws Exception;
}
