package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.common.concurrent.queue.mpmc.MpmcBlockingQueue;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.context.HttpRequestWrapper;
import cn.ray.gateway.core.helper.ResponseHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Ray
 * @date 2023/10/14 06:19
 * @description MPMC的核心实现处理器, 最终还是要使用NettyCoreProcessor
 */
@Slf4j
public class NettyMpMcProcessor implements NettyProcessor {

    private GatewayConfig gatewayConfig;

    private NettyCoreProcessor nettyCoreProcessor;

    private MpmcBlockingQueue<HttpRequestWrapper> mpmcBlockingQueue;

    private volatile boolean isRunning = false;

    private boolean isMultipleThread;

    private ExecutorService executorService;

    private Thread consumerProcessorThread;

    public NettyMpMcProcessor(GatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor, boolean isMultipleThread) {
        this.gatewayConfig = gatewayConfig;
        this.nettyCoreProcessor = nettyCoreProcessor;
        this.mpmcBlockingQueue = new MpmcBlockingQueue<>(gatewayConfig.getBufferSize());
        this.isMultipleThread = isMultipleThread;
    }

    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) throws Exception {
        System.err.println("NettyMpMcProcessor put!");
        this.mpmcBlockingQueue.put(httpRequestWrapper);
    }

    @Override
    public void start() {
        this.isRunning = true;
        this.nettyCoreProcessor.start();
        if(isMultipleThread) {
            this.executorService = Executors.newFixedThreadPool(gatewayConfig.getProcessThread());
            for(int i = 0; i < gatewayConfig.getProcessThread(); i ++) {
                this.executorService.submit(new ConsumerProcessor());
            }
        } else {
            this.consumerProcessorThread = new Thread(new ConsumerProcessor());
            this.consumerProcessorThread.start();
        }
    }

    @Override
    public void shutdown() {
        this.isRunning = false;
        this.nettyCoreProcessor.shutdown();
        if(isMultipleThread) {
            this.executorService.shutdown();
        }
    }

    /**
     * 消费者核心实现
     */
    public class ConsumerProcessor implements Runnable {

        @Override
        public void run() {
            while(isRunning) {
                HttpRequestWrapper event = null;
                try {
                    event = mpmcBlockingQueue.take();
                    nettyCoreProcessor.process(event);
                } catch (Throwable t) {
                    if(event != null) {
                        HttpRequest request = event.getFullHttpRequest();
                        ChannelHandlerContext ctx = event.getCtx();
                        try {
                            log.error("#ConsumerProcessor# onException 请求处理失败, request: {}. errorMessage: {}",
                                    request, t.getMessage(), t);

                            //	首先构建响应对象
                            FullHttpResponse fullHttpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                            //	判断是否保持连接
                            if(!HttpUtil.isKeepAlive(request)) {
                                ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                            } else {
                                //	如果保持连接, 则需要设置一下响应头：key: CONNECTION,  value: KEEP_ALIVE
                                fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                ctx.writeAndFlush(fullHttpResponse);
                            }

                        } catch (Exception e) {
                            //	ignore
                            log.error("#ConsumerProcessor# onException 请求回写失败, request: {}. errorMessage: {}",
                                    request, e.getMessage(), e);
                        }
                    } else {
                        log.error("#ConsumerProcessor# onException event is Empty errorMessage: {}",  t.getMessage(), t);
                    }
                }
            }
        }
    }
}
