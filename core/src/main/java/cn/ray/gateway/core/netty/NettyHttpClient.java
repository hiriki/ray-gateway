package cn.ray.gateway.core.netty;

import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.LifeCycle;
import cn.ray.gateway.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

/**
 * @author Ray
 * @date 2023/10/6 08:16
 * @description HTTP客户端启动类，主要用于下游HTTP请求服务的转发
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private AsyncHttpClient asyncHttpClient;

    private DefaultAsyncHttpClientConfig.Builder clientBuilder;

    private GatewayConfig gatewayConfig;

    private EventLoopGroup clientWorker;

    public NettyHttpClient(GatewayConfig gatewayConfig, EventLoopGroup clientWorker) {
        this.gatewayConfig = gatewayConfig;
        this.clientWorker = clientWorker;
        //	在构造函数调用初始化方法
        init();
    }

    @Override
    public void init() {
        this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(this.clientWorker)
                .setConnectTimeout(gatewayConfig.getHttpConnectTimeout())
                .setRequestTimeout(gatewayConfig.getHttpRequestTimeout())
                .setMaxConnections(gatewayConfig.getHttpMaxConnections())
                .setMaxConnectionsPerHost(gatewayConfig.getHttpConnectionsPerHost())
                .setMaxRequestRetry(gatewayConfig.getHttpMaxRequestRetry())
                .setPooledConnectionIdleTimeout(gatewayConfig.getHttpPooledConnectionIdleTimeout())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true);
    }

    @Override
    public void start() {
        this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if(asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                // ignore
                log.error("#NettyHttpClient.shutdown# shutdown error", e);
            }
        }
    }
}
