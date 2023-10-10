package cn.ray.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @author Ray
 * @date 2023/10/10 22:50
 * @description 请求包装类
 */
@Data
public class HttpRequestWrapper {

    private FullHttpRequest fullHttpRequest;

    private ChannelHandlerContext ctx;

}
