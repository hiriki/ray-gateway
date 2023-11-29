package cn.ray.gateway.core.netty.processor.filter;

import cn.ray.gateway.common.config.Rule;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.JSONUtil;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.netty.processor.cache.DefaultCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ray
 * @date 2023/11/28 23:48
 * @description 抽象的 Filter, 用于真正的Filter进行继承, 负责一些通用的逻辑操作, 包括加载缓存 Filter 配置信息以及 check Filter
 */
@Slf4j
public abstract class AbstractEntryProcessorFilter<FilterConfigClass> extends AbstractLinkedProcessorFilter<Context> {

    protected Filter filterAnnotation;

    /**
     * 每一个请求都会获取对应的过滤器配置信息，需要使用 caffeine 高性能缓存
     */
    protected Cache<String, FilterConfigClass> cache;

    protected final Class<FilterConfigClass> filterConfigClass;

    public AbstractEntryProcessorFilter(Class<FilterConfigClass> filterConfigClass) {
        this.filterAnnotation = this.getClass().getAnnotation(Filter.class);
        this.filterConfigClass = filterConfigClass;
        this.cache = DefaultCacheManager.getInstance().create(DefaultCacheManager.FILTER_CONFIG_CACHE_ID);
    }

    @Override
    public boolean check(Context context) throws Throwable {
        return context.getRule().hasFilterId(filterAnnotation.id());
    }

    @Override
    public void transformEntry(Context context, Object... args) throws Throwable {
        FilterConfigClass filterConfigClass = dynamicLoadFilterConfigCache(context, args);
        super.transformEntry(context, filterConfigClass);
    }

    /**
     * 动态加载缓存：每一个过滤器的具体配置规则
     * @param context
     * @param args
     * @return
     */
    private FilterConfigClass dynamicLoadFilterConfigCache(Context context, Object[] args) {
        // 通过上下文对象拿到规则，再通过规则获取到指定filterId的FilterConfig
        Rule.FilterConfig filterConfig = context.getRule().getFilterConfig(filterAnnotation.id());

        //	定义一个cacheKey：ruleId$filterId
        //  不同规则下filter的配置信息可能不一样，比如各个服务请求的超时时间可能不一致
        String ruleId = context.getRule().getId();
        String cacheKey = ruleId + BasicConstants.DOLLAR_SEPARATOR + filterAnnotation.id();

        FilterConfigClass filterConfigClass = cache.getIfPresent(cacheKey);

        if (filterConfigClass == null) {
            if (filterConfig != null && StringUtils.isEmpty(filterConfig.getConfig())) {
                String configStr = filterConfig.getConfig();
                try {
                    // 将配置信息 json 转为 对应的泛型类 filterConfigClass
                    filterConfigClass = JSONUtil.parse(configStr, this.filterConfigClass);
                    cache.put(cacheKey, filterConfigClass);
                } catch (Exception e) {
                    log.error("#AbstractEntryProcessorFilter# dynamicLoadCache filterId: {}, config parse error: {}",
                            filterAnnotation.id(),
                            configStr,
                            e);
                }
            }
        }
        return filterConfigClass;
    }
}
