package cn.ray.gateway.core.netty.processor.filter.error;

import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.GatewayBaseException;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.context.GatewayResponse;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;

/**
 * @author Ray
 * @date 2024/1/23 09:32
 * @description 默认异常处理过滤器, 写回异常响应
 */
@Filter(
        id = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID,
        name = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_NAME,
        type = ProcessorFilterType.ERROR,
        order = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ORDER
)
public class DefaultErrorFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public DefaultErrorFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {
            Throwable throwable = context.getThrowable();
            // 未知异常
            ResponseCode responseCode = ResponseCode.INTERNAL_ERROR;

            // 网关自定义异常
            if (throwable instanceof GatewayBaseException) {
                GatewayBaseException gatewayBaseException = (GatewayBaseException) throwable;
                responseCode = gatewayBaseException.getCode();
            }

            GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponse(responseCode);
            context.setResponse(gatewayResponse);
        } finally {
            System.err.println("============> do error filter <===============");
            // 设置写回标记
            context.written();
            // 触发后续过滤器执行
            super.fireNext(context, args);
        }
    }
}
