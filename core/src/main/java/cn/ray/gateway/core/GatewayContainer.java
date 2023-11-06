package cn.ray.gateway.core;

import cn.ray.gateway.common.constants.GatewayBufferHelper;
import cn.ray.gateway.core.netty.NettyHttpClient;
import cn.ray.gateway.core.netty.NettyHttpServer;
import cn.ray.gateway.core.netty.processor.NettyBatchEventProcessor;
import cn.ray.gateway.core.netty.processor.NettyCoreProcessor;
import cn.ray.gateway.core.netty.processor.NettyMpMcProcessor;
import cn.ray.gateway.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ray
 * @date 2023/10/6 08:13
 * @description 主流程的容器类
 */
@Slf4j
public class GatewayContainer implements LifeCycle {

    /**
     * 核心配置类
     */
    private final GatewayConfig gatewayConfig;

    /**
     * 接收http请求的server
     */
    private NettyHttpServer nettyHttpServer;

    /**
     * http转发的核心类
     */
    private NettyHttpClient nettyHttpClient;

    /**
     * 核心处理器
     */
    private NettyProcessor nettyProcessor;

    public GatewayContainer(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        init();
    }

    @Override
    public void init() {
        //	1. 构建核心处理器
        NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();

        //	2. 是否开启缓存
        String bufferType = gatewayConfig.getBufferType();

        if (GatewayBufferHelper.isFlusher(bufferType)) {
            this.nettyProcessor = new NettyBatchEventProcessor(gatewayConfig,nettyCoreProcessor);
        } else if (GatewayBufferHelper.isMpmc(bufferType)) {
            this.nettyProcessor = new NettyMpMcProcessor(gatewayConfig,nettyCoreProcessor,false);
        } else {
            this.nettyProcessor = nettyCoreProcessor;
        }

        //	3. 创建 NettyHttpServer
        this.nettyHttpServer = new NettyHttpServer(gatewayConfig,this.nettyProcessor);

        //	4. 创建NettyHttpClient，复用 server worker EventLoopGroup，减少上下文切换
        nettyHttpClient = new NettyHttpClient(gatewayConfig, nettyHttpServer.getWorker());
    }

    @Override
    public void start() {
        this.nettyProcessor.start();
        this.nettyHttpServer.start();
        this.nettyHttpClient.start();
        log.info("GatewayContainer started !");
    }

    @Override
    public void shutdown() {
        this.nettyProcessor.shutdown();
        this.nettyHttpServer.shutdown();
        this.nettyHttpClient.shutdown();
    }
}
