package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.context.HttpRequestWrapper;

/**
 * @author Ray
 * @date 2023/10/14 06:15
 * @description flusher缓冲队列的核心实现, 最终调用的方法还是要回归到NettyCoreProcessor
 */
public class NettyBatchEventProcessor implements NettyProcessor {

    private GatewayConfig gatewayConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    public NettyBatchEventProcessor(GatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
