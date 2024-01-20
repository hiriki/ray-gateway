package cn.ray.gateway.core.netty.processor.filter.route;

import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.GatewayConnectException;
import cn.ray.gateway.common.exception.GatewayResponseException;
import cn.ray.gateway.core.GatewayConfigLoader;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.context.GatewayResponse;
import cn.ray.gateway.core.helper.AsyncHttpHelper;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @author Ray
 * @date 2024/1/21 03:09
 * @description 中置过滤器-HTTP请求转发
 */
@Slf4j
@Filter(
        id = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID,
        name = ProcessorFilterConstants.HTTP_ROUTE_FILTER_NAME,
        type = ProcessorFilterType.ROUTE,
        order = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ORDER
)
public class HttpRouteFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public HttpRouteFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        GatewayContext gatewayContext = (GatewayContext) context;
        Request request = gatewayContext.getRequestMutable().build();

        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);

        // 单异步和双异步模式
        boolean whenComplete = GatewayConfigLoader.gatewayConfig().isWhenComplete();

        if (whenComplete) {
            // 单异步
            future.whenComplete(((response, throwable) -> {
                complete(request, response, throwable, gatewayContext, args);
            }));
        } else {
            // 双异步
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request, response, throwable, gatewayContext, args);
            }));
        }
    }

    /**
     * 真正执行请求响应回来的操作方法
     * @param request
     * @param response
     * @param throwable
     * @param gatewayContext
     * @param args
     */
    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext, Object[] args) {
        try {
            //	1. 释放请求资源
            gatewayContext.releaseRequest();

            //	2. 判断是否有异常产生
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();

                //  超时异常
                if (throwable instanceof TimeoutException) {
                    log.warn("#HttpRouteFilter# complete 返回响应执行，请求路径：{}，耗时超过 {}  ms.",
                            url,
                            (request.getRequestTimeout() == 0 ?
                                    GatewayConfigLoader.gatewayConfig().getHttpRequestTimeout() :
                                    request.getRequestTimeout())
                    );

                    // 网关上下文中保存异常信息
                    gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    // 其他异常情况
                    gatewayContext.setThrowable(new GatewayConnectException(throwable,
                            gatewayContext.getUniqueId(),
                            url,
                            ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                //	正常返回响应结果, 设置响应信息
                gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Throwable t) {
            //	最终兜底异常处理, 可能存在处理响应的异常
            gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("#HttpRouteFilter# complete 出现未知异常", t);
        } finally {
            try {
                //	1.	设置写回标记
                gatewayContext.written();

                //	2. 	让异步线程内部自己进行触发下一个节点执行
                super.fireNext(gatewayContext, args);
            } catch (Throwable t) {
                //	兜底处理，把异常信息放入上下文
                gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
                log.error("#HttpRouteFilter# fireNext 出现异常", t);
            }
        }
    }
}
