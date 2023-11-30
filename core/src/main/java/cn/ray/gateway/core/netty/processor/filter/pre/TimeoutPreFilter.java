package cn.ray.gateway.core.netty.processor.filter.pre;

import cn.ray.gateway.common.config.ServiceInvoker;
import cn.ray.gateway.common.constants.GatewayProtocol;
import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.core.context.AttributeKey;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.context.GatewayRequest;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ray
 * @date 2023/11/30 19:54
 * @description 前置过滤器-超时时间设置
 */
@Filter(
        id = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID,
        name = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_NAME,
        type = ProcessorFilterType.PRE,
        order = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ORDER
)
public class TimeoutPreFilter extends AbstractEntryProcessorFilter<TimeoutPreFilter.Config> {

    public TimeoutPreFilter() {
        super(TimeoutPreFilter.Config.class);
    }

    /**
     * 设置下游请求的超时时间
     * @param context
     * @param args
     * @throws Throwable
     */
    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {
            GatewayContext gatewayContext = (GatewayContext) context;
            String protocol = gatewayContext.getProtocol();
            Config config = (Config) args[0];
            switch (protocol) {
                case GatewayProtocol.HTTP:
                    GatewayRequest gatewayRequest = gatewayContext.getRequest();
                    gatewayRequest.setRequestTimeout(config.getTimeout());
                    break;
                case GatewayProtocol.DUBBO:
                    ServiceInvoker dubboServiceInvoker = gatewayContext.getAttribute(AttributeKey.DUBBO_INVOKER);
                    dubboServiceInvoker.setTimeout(config.getTimeout());
                    break;
                default:
                    break;
            }
        } finally {
            // 驱动过滤器链
            super.fireNext(context, args);
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private Integer timeout;
    }
}
