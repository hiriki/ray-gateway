package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.core.context.HttpRequestWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author Ray
 * @date 2023/10/10 22:56
 * @description
 */
public class NettyCoreProcessor implements NettyProcessor {

    @Override
    public void process(HttpRequestWrapper event) {
        FullHttpRequest request = event.getFullHttpRequest();
        ChannelHandlerContext ctx = event.getCtx();
        try {
            //	1. 解析FullHttpRequest, 把他转换为我们自己想要的内部对象：Context

            //	2. 执行整个的过滤器逻辑：FilterChain

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
