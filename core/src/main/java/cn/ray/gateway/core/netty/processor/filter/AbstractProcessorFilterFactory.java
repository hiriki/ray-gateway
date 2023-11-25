package cn.ray.gateway.core.netty.processor.filter;

import cn.ray.gateway.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date 2023/11/25 23:31
 * @description 抽象的过滤器工厂类, 提供基础的构建过滤器链和获取指定过滤器的方法
 */
@Slf4j
public abstract class AbstractProcessorFilterFactory implements ProcessorFilterFactory {

    /**
     * pre + router + post
     */
    public DefaultProcessorFilterChain defaultProcessorFilterChain = new DefaultProcessorFilterChain("defaultProcessorFilterChain");

    /**
     * error + post
     */
    public DefaultProcessorFilterChain errorProcessorFilterChain = new DefaultProcessorFilterChain("errorProcessorFilterChain");

    /**
     * 根据过滤器类型存储的filter集合
     */
    public Map<String/* ProcessorFilterType.code */, Map<String /* filterId */, ProcessorFilter<Context>>> processorFilterTypeMap = new LinkedHashMap<>();

    /**
     * 根据过滤器ID存储的filter集合
     */
    public Map<String /* filterId */, ProcessorFilter<Context>> processorFilterIdMap = new LinkedHashMap<>();

    @Override
    public void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception {
        switch (filterType) {
            case PRE:
            case ROUTE:
                addFilterForChain(defaultProcessorFilterChain, filters);
                break;
            case ERROR:
                addFilterForChain(errorProcessorFilterChain, filters);
                break;
            case POST:
                addFilterForChain(defaultProcessorFilterChain, filters);
                addFilterForChain(errorProcessorFilterChain, filters);
                break;
            default:
                throw new RuntimeException("ProcessorFilterType is not supported !");
        }
    }

    private void addFilterForChain(DefaultProcessorFilterChain processorFilterChain, List<ProcessorFilter<Context>> filters) throws Exception {
        for (ProcessorFilter<Context> filter : filters) {
            filter.init();
            doBuilder(processorFilterChain, filter);
        }
    }

    /**
     * 往过滤器链中添加指定过滤器
     * @param processorFilterChain
     * @param filter
     */
    private void doBuilder(DefaultProcessorFilterChain processorFilterChain, ProcessorFilter<Context> filter) {
        log.info("filterChain: {}, the scanner filter is : {}", processorFilterChain.getId(), filter.getClass().getName());

        Filter annotation = filter.getClass().getAnnotation(Filter.class);

        if (annotation != null) {
            // 构建过滤器链: 添加过滤器
            processorFilterChain.addLast((AbstractLinkedProcessorFilter<Context>) filter);

            // 映射到过滤器集合中
            String filterId = annotation.id();
            // 冗余设计, 后续可不用给定FilterID, 默认为类名
            if (filterId == null || filterId.length() < 1) {
                filterId = filter.getClass().getName();
            }

            String code = annotation.type().getCode();
            Map<String, ProcessorFilter<Context>> filterMap = processorFilterTypeMap.get(code);
            if (filterMap == null) {
                filterMap = new LinkedHashMap<>();
            }
            filterMap.put(filterId, filter);

            // type
            processorFilterTypeMap.put(code, filterMap);

            // id
            processorFilterIdMap.put(filterId, filter);
        }
    }

    @Override
    public <T> T getFilter(Class<T> clazz) throws Exception {
        Filter annotation = clazz.getAnnotation(Filter.class);
        if (annotation != null) {
            String filterId = annotation.id();
            // 冗余设计, 后续可不用给定FilterID, 默认为类名
            if (filterId == null || filterId.length() < 1) {
                filterId = clazz.getName();
            }
            return this.getFilter(filterId);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFilter(String filterId) throws Exception {
        ProcessorFilter<Context> filter = null;
        if (!processorFilterIdMap.isEmpty()) {
            filter = processorFilterIdMap.get(filterId);
        }
        return (T)filter;
    }
}
