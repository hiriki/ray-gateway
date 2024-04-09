package cn.ray.gateway.core.netty.processor.filter.pre;

import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;

/**
 * @author Ray
 * @date 2024/4/9 19:32
 * @description
 */
@Filter(
        id = ProcessorFilterConstants.GRAY_RELEASE_PRE_FILTER_ID,
        name = ProcessorFilterConstants.GRAY_RELEASE_PRE_FILTER_NAME,
        type = ProcessorFilterType.PRE,
        order = ProcessorFilterConstants.GRAY_RELEASE_PRE_FILTER_ORDER
)
public class GrayReleasePreFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public static final String GRAY = "gray";

    public GrayReleasePreFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {
            GatewayContext gatewayContext = (GatewayContext) context;
            // 过滤选取灰度流量
            String s = gatewayContext.getRequest().getHeaders().get(GRAY);
            if (s!=null&&!s.isEmpty()) {
                gatewayContext.setGray(true);
                return;
            }

//            // 选取部分的灰度用户
//            String clientIp = gatewayContext.getRequest().getClientIp();
//            // 等价于对1024取模
//            int res = clientIp.hashCode() & (1024 - 1);
//            if (res == 1) {
//                //1024分之一的概率
//                gatewayContext.setGray(true);
//            }
        } finally {
            super.fireNext(context, args);;
        }
    }
}
