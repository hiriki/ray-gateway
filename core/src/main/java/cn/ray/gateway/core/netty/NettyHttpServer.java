package cn.ray.gateway.core.netty;

import cn.ray.gateway.common.utils.NetUtil;
import cn.ray.gateway.common.utils.RemotingHelper;
import cn.ray.gateway.common.utils.RemotingUtil;
import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.LifeCycle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author Ray
 * @date 2023/10/6 08:15
 * @description 承接所有网络请求的核心类
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {

    private final GatewayConfig gatewayConfig;

    private int port = 8888;

    private ServerBootstrap bootstrap;

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    public NettyHttpServer(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
        if (gatewayConfig.getPort() > 0 && gatewayConfig.getPort() < 65535) {
            this.port = gatewayConfig.getPort();
        }
        // 初始化NettyHttpServer
        init();
    }

    @Override
    public void init() {
        this.bootstrap = new ServerBootstrap();
        if(useEpoll()) {
            this.boss = new EpollEventLoopGroup(gatewayConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("NettyBossEPoll"));
            this.worker = new EpollEventLoopGroup(gatewayConfig.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("NettyWorkerEPoll"));
        } else {
            this.boss = new NioEventLoopGroup(gatewayConfig.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("NettyBossNio"));
            this.worker = new NioEventLoopGroup(gatewayConfig.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory("NettyWorkerNio"));
        }
    }

    /**
     * 服务器启动方法
     */
    @Override
    public void start() {
        ServerBootstrap serverBootstrap = this.bootstrap
                .group(boss, worker)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)     // syn + accept = backlog
                .option(ChannelOption.SO_REUSEADDR, true)   // tcp端口重绑定
                .option(ChannelOption.SO_KEEPALIVE, false)  // 如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.TCP_NODELAY, true)   // 开启Nagle算法，使用小数据传输时延迟发送
                .childOption(ChannelOption.SO_SNDBUF, 65535)    // 设置发送数据缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, 65535)    // 设置接收数据缓冲区大小
                .localAddress(new InetSocketAddress(this.port))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(
                                new HttpServerCodec(),
                                // 用于将 HTTP 消息的多个部分聚合成一个完整的 FullHttpRequest 或 FullHttpResponse 对象
                                new HttpObjectAggregator(gatewayConfig.getMaxContentLength()),
                                new HttpServerExpectContinueHandler(),
                                // 检查请求中是否包含了 Expect: 100-continue 头部
                                // 如果是，它会向客户端发送一个 100 Continue 响应，表示服务器愿意接受请求体
                                // 举个例子，假设客户端要上传一个大文件到服务器，但在上传之前，它希望服务器先确认是否接受
                                // 客户端会发送一个带有 Expect: 100-continue 头部的请求
                                // 如果服务器愿意接受，它会返回 100 Continue 响应码，然后客户端就可以继续发送文件数据。
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler()
                        );
                    }
                });

        if(gatewayConfig.isNettyAllocator()) {
            serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            this.bootstrap.bind().sync();
            log.info("< ============= Gateway Server StartUp On Port: " + this.port + "================ >");
        } catch (Exception e) {
            throw new RuntimeException("this.bootstrap.bind().sync() fail!", e);
        }
    }

    @Override
    public void shutdown() {
        if(boss!=null) {
            boss.shutdownGracefully();
        }
        if(worker!=null) {
            worker.shutdownGracefully();
        }
    }

    /**
     * 判断是否支持EPoll
     * @return
     */
    public boolean useEpoll() {
        // 只有 Linux 才能开启 epoll
        return gatewayConfig.isUseEpoll() && RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    /**
     * 连接管理器
     */
    static class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddr);
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY SERVER PIPELINE: channelUnregistered {}", remoteAddr);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY SERVER PIPELINE: channelActive {}", remoteAddr);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY SERVER PIPELINE: channelInactive {}", remoteAddr);
            super.channelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent)evt;
                if(event.state().equals(IdleState.ALL_IDLE)) {
                    final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.warn("NETTY SERVER PIPELINE: userEventTriggered: IDLE {}", remoteAddr);
                    ctx.channel().close();
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.warn("NETTY SERVER PIPELINE: remoteAddr： {}, exceptionCaught {}", remoteAddr, cause);
            ctx.channel().close();
        }

    }
}
