package cn.ray.gateway.core.netty.processor.filter;

import cn.ray.gateway.common.utils.ServiceLoader;
import cn.ray.gateway.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Ray
 * @date 2023/11/27 08:58
 * @description 默认过滤器工厂实现类
 */
@Slf4j
public class DefaultProcessorFilterFactory extends AbstractProcessorFilterFactory {

    private static class SingletonHolder {
        private static final DefaultProcessorFilterFactory INSTANCE = new DefaultProcessorFilterFactory();
    }

    public static DefaultProcessorFilterFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 通过 SPI 方式加载所有的 ProcessorFilter 的实现
    @SuppressWarnings("unchecked")
    private DefaultProcessorFilterFactory() {

        Map<String, List<ProcessorFilter<Context>>> filterMap = new LinkedHashMap<>();

        //	通过ServiceLoader加载
        @SuppressWarnings("rawtypes")
        ServiceLoader<ProcessorFilter> serviceLoader = ServiceLoader.load(ProcessorFilter.class);

        for (ProcessorFilter<Context> filter : serviceLoader) {
            Filter annotation = filter.getClass().getAnnotation(Filter.class);

            if (annotation != null) {
                String filterType = annotation.type().getCode();
                List<ProcessorFilter<Context>> filters = filterMap.get(filterType);
                if (filters == null) {
                    filters = new ArrayList<>();
                }
                filters.add(filter);
                filterMap.put(filterType, filters);
            }
        }

        for (ProcessorFilterType filterType : ProcessorFilterType.values()) {
            List<ProcessorFilter<Context>> filters = filterMap.get(filterType);
            if (filters == null || filters.isEmpty()) {
                continue;
            }

            // 通过注解 Filter order 值 对集合中的过滤器从小到大排序
            Collections.sort(filters, new Comparator<ProcessorFilter<Context>>() {
                @Override
                public int compare(ProcessorFilter<Context> o1, ProcessorFilter<Context> o2) {
                    return o1.getClass().getAnnotation(Filter.class).order() -
                            o2.getClass().getAnnotation(Filter.class).order();
                }
            });

            try {
                super.buildFilterChain(filterType, filters);
            } catch (Exception e) {
                // ignore
                log.error("#DefaultProcessorFilterFactory.buildFilterChain# 网关过滤器加载异常, 异常信息为：{}!",e.getMessage(), e);
            }
        }
    }

    /**
     * 正常的过滤器链执行: pre + router + post
     * @param context
     * @throws Exception
     */
    @Override
    public void doFilterChain(Context context) throws Exception {
        try {
            defaultProcessorFilterChain.entry(context);
        } catch (Throwable t) {
            log.error("#DefaultProcessorFilterFactory.doFilterChain# ERROR MESSAGE: {}" , t.getMessage(), t);

            // 上下文设置异常
            context.setThrowable(t);

            // 执行 doFilterChain 显式抛出异常时，Context上下文的生命周期为：Context.TERMINATED
            // 恢复为正常运行状态, 即转换过滤器链
            if(context.isTerminated()) {
                context.running();
            }

            // 执行异常过滤器链
            doErrorFilterChain(context);
        }
    }

    /**
     * 异常的过滤器链执行: error + post
     * @param context
     * @throws Exception
     */
    @Override
    public void doErrorFilterChain(Context context) throws Exception {
        try {
            errorProcessorFilterChain.entry(context);
        } catch (Throwable t) {
            log.error("#DefaultProcessorFilterFactory.doErrorFilterChain# ERROR MESSAGE: {}" , t.getMessage(), t);
        }
    }
}
