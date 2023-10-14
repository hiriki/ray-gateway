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
                .setFollowRedirect(false)   // 禁用自动重定向
                .setEventLoopGroup(this.clientWorker) // 事件循环组，用于处理客户端的网络事件
                .setConnectTimeout(gatewayConfig.getHttpConnectTimeout()) // 连接超时时间
                .setRequestTimeout(gatewayConfig.getHttpRequestTimeout()) // 请求超时时间
                .setMaxConnections(gatewayConfig.getHttpMaxConnections()) // 允许的最大连接数
                .setMaxConnectionsPerHost(gatewayConfig.getHttpConnectionsPerHost()) // 每个主机允许的最大连接数
                .setMaxRequestRetry(gatewayConfig.getHttpMaxRequestRetry()) // 最大的请求重试次数
                .setPooledConnectionIdleTimeout(gatewayConfig.getHttpPooledConnectionIdleTimeout()) // 池化连接的空闲超时时间，即在连接池中保持连接的最大空闲时间
                .setAllocator(PooledByteBufAllocator.DEFAULT)   // 使用 Netty 的内存池来分配内存
                .setCompressionEnforced(true); // 启用压缩功能，客户端会发送支持压缩的请求
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
