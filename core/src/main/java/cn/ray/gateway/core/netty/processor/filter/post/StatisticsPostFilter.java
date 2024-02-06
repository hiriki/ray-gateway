package cn.ray.gateway.core.netty.processor.filter.post;

import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ray
 * @date 2024/2/6 19:35
 * @description 后置过滤器-统计分析
 */
@Filter(
        id = ProcessorFilterConstants.STATISTICS_POST_FILTER_ID,
        name = ProcessorFilterConstants.STATISTICS_POST_FILTER_NAME,
        type = ProcessorFilterType.POST,
        order = ProcessorFilterConstants.STATISTICS_POST_FILTER_ORDER
)
public class StatisticsPostFilter extends AbstractEntryProcessorFilter<StatisticsPostFilter.Config> {

    public StatisticsPostFilter() {
        super(StatisticsPostFilter.Config.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {

        } finally {
            context.terminated();
            super.fireNext(context, args);
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private boolean useRollingNumber = true;
    }
}
