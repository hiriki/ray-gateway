package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.context.HttpRequestWrapper;

/**
 * @author Ray
 * @date 2023/10/14 06:19
 * @description MPMC的核心实现处理器, 最终还是要使用NettyCoreProcessor
 */
public class NettyMpMcProcessor implements NettyProcessor {

    private GatewayConfig gatewayConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    public NettyMpMcProcessor(GatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
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
