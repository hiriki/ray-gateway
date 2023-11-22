package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.context.HttpRequestWrapper;
import cn.ray.gateway.core.helper.RequestHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author Ray
 * @date 2023/10/10 22:56
 * @description 核心处理类，调用执行过滤器逻辑
 */
public class NettyCoreProcessor implements NettyProcessor {

    @Override
    public void process(HttpRequestWrapper event) {
        FullHttpRequest fullHttpRequest = event.getFullHttpRequest();
        ChannelHandlerContext ctx = event.getCtx();
        try {
            //	1. 解析FullHttpRequest, 把他转换为我们自己想要的内部对象：Context
            GatewayContext gatewayContext = RequestHelper.doContext(fullHttpRequest, ctx);
            //	2. 执行整个的过滤器逻辑：FilterChain
            System.err.println("----- 进入过滤器逻辑 -----");
        } catch (Throwable t) {
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
