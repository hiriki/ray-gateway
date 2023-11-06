package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.common.concurrent.queue.flusher.ParallelFlusher;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.context.HttpRequestWrapper;
import cn.ray.gateway.core.helper.ResponseHelper;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ray
 * @date 2023/10/14 06:15
 * @description flusher缓冲队列的核心实现, 最终调用的方法还是要回归到NettyCoreProcessor
 */
@Slf4j
public class NettyBatchEventProcessor implements NettyProcessor {

    private static final String THREAD_NAME_PREFIX = "gateway-flusher-";

    private GatewayConfig gatewayConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    private ParallelFlusher<HttpRequestWrapper> parallelFlusher;

    public NettyBatchEventProcessor(GatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
                .setBufferSize(gatewayConfig.getBufferSize())
                .setThreadSize(gatewayConfig.getProcessThread())
                .setThreadNamePrefix(THREAD_NAME_PREFIX)
                .setProducerType(ProducerType.MULTI)
                .setWaitStrategy(gatewayConfig.getUseWaitStrategy());

        BatchEventProcessorListener batchEventProcessorListener = new BatchEventProcessorListener();
        builder.setEventListener(batchEventProcessorListener);

        this.parallelFlusher = builder.build();
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        System.err.println("NettyBatchEventProcessor add!");
        parallelFlusher.add(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.nettyCoreProcessor.start();
        this.parallelFlusher.start();
    }

    @Override
    public void shutdown() {
        this.nettyCoreProcessor.shutdown();
        this.parallelFlusher.shutdown();
    }

    public class BatchEventProcessorListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {
        @Override
        public void onEvent(HttpRequestWrapper event) {
            nettyCoreProcessor.process(event);
        }

        @Override
        public void onException(Throwable ex, long sequence, HttpRequestWrapper event) {
            HttpRequest request = event.getFullHttpRequest();
            ChannelHandlerContext ctx = event.getCtx();
            try {
                log.error("#BatchEventProcessorListener# onException 请求处理失败, request: {}. errorMessage: {}",
                        request, ex.getMessage(), ex);
                // 构造响应对象
                FullHttpResponse response = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                // 判断是否长连接
                if (!HttpUtil.isKeepAlive(request)) {
                    // 写出响应并在操作完成后关闭通道
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    // 如果长连接, 则需要设置一下响应头：key: CONNECTION,  value: KEEP_ALIVE
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(response);
                }
            } catch (Exception e) {
                //	ignore
                log.error("#BatchEventProcessorListener# onException 请求回写失败, request: {}. errorMessage: {}",
                        request, e.getMessage(), e);
            }
        }
    }

    public GatewayConfig getGatewayConfig() {
        return this.gatewayConfig;
    }
}
