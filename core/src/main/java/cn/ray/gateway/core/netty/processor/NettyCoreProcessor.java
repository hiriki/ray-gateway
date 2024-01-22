package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.GatewayNotFoundException;
import cn.ray.gateway.common.exception.GatewayPathNoMatchedException;
import cn.ray.gateway.common.exception.GatewayResponseException;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.context.HttpRequestWrapper;
import cn.ray.gateway.core.helper.RequestHelper;
import cn.ray.gateway.core.helper.ResponseHelper;
import cn.ray.gateway.core.netty.processor.filter.DefaultProcessorFilterFactory;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ray
 * @date 2023/10/10 22:56
 * @description 核心处理类，调用执行过滤器逻辑
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor {

    private ProcessorFilterFactory processorFilterFactory = DefaultProcessorFilterFactory.getInstance();

    @Override
    public void process(HttpRequestWrapper event) {
        FullHttpRequest fullHttpRequest = event.getFullHttpRequest();
        ChannelHandlerContext ctx = event.getCtx();
        try {
            //	1. 解析FullHttpRequest, 把他转换为我们自己想要的内部对象：Context
            GatewayContext gatewayContext = RequestHelper.doContext(fullHttpRequest, ctx);

            //	2. 执行整个的过滤器逻辑：FilterChain
            processorFilterFactory.doFilterChain(gatewayContext);

        } catch (GatewayPathNoMatchedException e) {
            log.error("#NettyCoreProcessor# process 网关请求路径不匹配，快速失败： code: {}, msg: {}",
                    e.getCode().getCode(), e.getCode().getMessage(), e);
            FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
            //	释放资源写回响应
            doWriteAndRelease(ctx, fullHttpRequest, response);
        } catch (GatewayNotFoundException e) {
            log.error("#NettyCoreProcessor# process 网关资源未找到： code: {}, msg: {}",
                    e.getCode().getCode(), e.getCode().getMessage(), e);
            FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
            //	释放资源写回响应
            doWriteAndRelease(ctx, fullHttpRequest, response);
        } catch (GatewayResponseException e) {
            log.error("#NettyCoreProcessor# process 网关响应异常： code: {}, msg: {}",
                    e.getCode().getCode(), e.getCode().getMessage(), e);
            FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
            //	释放资源写回响应
            doWriteAndRelease(ctx, fullHttpRequest, response);
        } catch (Throwable t) {
            log.error("#NettyCoreProcessor# process 网关内部未知错误异常", t);
            FullHttpResponse response = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            //	释放资源写回响应
            doWriteAndRelease(ctx, fullHttpRequest, response);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    /**
     * 写回响应信息并释放资源
     * @param ctx
     * @param request
     * @param response
     */
    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        // 异常时可能存在资源未释放的情况
        boolean release = ReferenceCountUtil.release(request);
        if(!release) {
            log.warn("#NettyCoreProcessor# doWriteAndRelease release fail 释放资源失败， request:{}, release:{}",
                    request.uri(),
                    release);
        }
    }
}
