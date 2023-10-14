package cn.ray.gateway.core;

import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.NetUtil;
import lombok.Data;

/**
 * @author Ray
 * @date 2023/10/6 07:04
 * @description 网关的通用配置信息类
 */
@Data
public class GatewayConfig {

    /**
     * 网关的默认端口
     */
    private int port = 8888;

    /**
     * 网关服务唯一ID：gatewayId  IP:端口
     */
    private String gatewayId = NetUtil.getLocalIp() + BasicConstants.COLON_SEPARATOR + port;

    /**
     * 网关的注册中心地址 etcd
     */
    private String registerAddress = "http://192.168.11.114:2379,http://192.168.11.115:2379,http://192.168.11.116:2379";

    /**
     * 网关的命名空间：dev test prod
     */
    private String namespace = "gateway-dev";

    /**
     * 网关服务器的 CPU 核数映射的线程数
     */
    private int processThread = Runtime.getRuntime().availableProcessors();

    /**
     * Netty 的 Boss 线程数
     */
    private int eventLoopGroupBossNum = 1;

    /**
     * Netty 的 Worker 线程数
     */
    private int eventLoopGroupWorkerNum = processThread;

    /**
     * 是否开启EPOLL
     */
    private boolean useEpoll = true;

    /**
     * 是否开启Netty内存分配机制
     */
    private boolean nettyAllocator = true;

    /**
     * Http Body 报文最大大小
     */
    private int maxContentLength = 64 * 1024 * 1024;

    /**
     * dubbo开启连接数数量
     */
    private int dubboConnections = processThread;

    /**
     * 设置响应模式, 默认是单异步模式：CompletableFuture回调处理结果： whenComplete  or  whenCompleteAsync
     */
    private boolean whenComplete = true;

    /**
     * 网关队列：缓冲模式
     */
    private String bufferType = ""; // GatewayBufferHelper.FLUSHER

    /**
     * 网关队列：内存队列大小
     */
    private int bufferSize = 1024 * 16;

    /**
     * 网关队列：阻塞/等待策略
     */
    private String waitStrategy = "blocking";


    //  Http Async 参数选项：

    /**
     * 连接超时时间
     */
    private int httpConnectTimeout = 30 * 1000;

    /**
     * 请求超时时间
     */
    private int httpRequestTimeout = 30 * 1000;

    /**
     * 客户端请求重试次数
     */
    private int httpMaxRequestRetry = 2;

    /**
     * 客户端请求最大连接数
     */
    private int httpMaxConnections = 10000;

    /**
     * 客户端每个地址支持的最大连接数
     */
    private int httpConnectionsPerHost = 8000;

    /**
     * 客户端空闲连接超时时间, 默认60秒
     */
    private int httpPooledConnectionIdleTimeout = 60 * 1000;
}
