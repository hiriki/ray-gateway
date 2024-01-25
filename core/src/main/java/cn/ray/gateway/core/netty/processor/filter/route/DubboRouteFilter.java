package cn.ray.gateway.core.netty.processor.filter.route;

import cn.ray.gateway.common.config.DubboServiceInvoker;
import cn.ray.gateway.common.config.ServiceInvoker;
import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.DubboConnectException;
import cn.ray.gateway.common.exception.GatewayResponseException;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.core.GatewayConfigLoader;
import cn.ray.gateway.core.context.*;
import cn.ray.gateway.core.helper.DubboReferenceHelper;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Ray
 * @date 2024/1/26 05:56
 * @description 中置过滤器-Dubbo请求转发
 */
@Slf4j
@Filter(
        id = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ID,
        name = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_NAME,
        type = ProcessorFilterType.ROUTE,
        order = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ORDER
)
public class DubboRouteFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public DubboRouteFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        GatewayContext gatewayContext = (GatewayContext) context;
        ServiceInvoker serviceInvoker = gatewayContext.getRequiredAttribute(AttributeKey.DUBBO_INVOKER);
        DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker) serviceInvoker;

        // 请求体类型校验, 只接受JSON格式
        if (!HttpHeaderValues.APPLICATION_JSON.toString().equals(gatewayContext.getOriginRequest().getContentType())) {
            // 显式抛出异常, 终止执行
            gatewayContext.terminated();
            throw new GatewayResponseException(ResponseCode.DUBBO_PARAMETER_VALUE_ERROR);
        }

        String body = gatewayContext.getOriginRequest().getBody();

        // 释放请求资源
        gatewayContext.releaseRequest();

        List<Object> parameters = null;

        try {
            parameters = FastJsonConvertUtil.convertJSONToArray(body, Object.class);
        } catch (Exception e) {
            // 解析异常, 无法处理, 终止执行
            gatewayContext.terminated();
            throw new GatewayResponseException(ResponseCode.DUBBO_PARAMETER_VALUE_ERROR);
        }

        // 构建dubbo请求对象
        DubboRequest dubboRequest = DubboReferenceHelper.buildDubboRequest(dubboServiceInvoker, parameters.toArray());

        CompletableFuture<Object> future = DubboReferenceHelper.getInstance().$invokeAsync(gatewayContext, dubboRequest);

        // 单异步和双异步模式
        boolean whenComplete = GatewayConfigLoader.gatewayConfig().isWhenComplete();

        if (whenComplete) {
            // 单异步
            future.whenComplete(((response, throwable) -> {
                complete(dubboRequest, response, throwable, gatewayContext, args);
            }));
        } else {
            // 双异步
            future.whenCompleteAsync(((response, throwable) -> {
                complete(dubboRequest, response, throwable, gatewayContext, args);
            }));
        }
    }

    private void complete(DubboRequest dubboRequest,
                          Object response, Throwable throwable,
                          GatewayContext gatewayContext, Object[] args) {
        try {
            if (Objects.nonNull(throwable)) {
                DubboConnectException dubboConnectException = new DubboConnectException(throwable,
                        gatewayContext.getUniqueId(),
                        gatewayContext.getOriginRequest().getPath(),
                        dubboRequest.getInterfaceClass(),
                        dubboRequest.getMethodName(),
                        ResponseCode.DUBBO_RESPONSE_ERROR);
                gatewayContext.setThrowable(dubboConnectException);
            } else {
                GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponseObj(response);
                gatewayContext.setResponse(gatewayResponse);
            }
        } catch (Throwable t) {
            //	最终兜底异常处理
            gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("#DubboRouteFilter# complete 出现未知异常", t);
        } finally {
            try {
                //	1.	设置写回标记
                gatewayContext.written();
                //	2. 	让异步线程内部自己进行触发下一个节点执行
                super.fireNext(gatewayContext, args);
            } catch (Throwable t) {
                //	兜底处理，把异常信息放入上下文
                gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
                log.error("#DubboRouteFilter# fireNext 出现异常", t);
            }
        }
    }
}
