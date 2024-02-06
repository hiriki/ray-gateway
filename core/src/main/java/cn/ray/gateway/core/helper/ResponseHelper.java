package cn.ray.gateway.core.helper;

import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.utils.TimeUtil;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.context.GatewayResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

import java.util.Objects;

/**
 * @author Ray
 * @date 2023/11/3 08:34
 * @description 响应辅助类
 */
public class ResponseHelper {

    /**
     * 根据响应枚举构建响应
     * @param responseCode
     * @return io.netty.handler.codec.http
     */
    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponse(responseCode);
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                responseCode.getStatus(),
                Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes()));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        return httpResponse;
    }

    /**
     * 通过上下文对象和 GatewayResponse 对象构建 FullHttpResponse
     * @param context
     * @param gatewayResponse
     * @return
     */
    private static FullHttpResponse getHttpResponse(Context context, GatewayResponse gatewayResponse) {
        ByteBuf content;
        if(Objects.nonNull(gatewayResponse.getFutureResponse())) {  // 存在响应对象
            content = Unpooled.wrappedBuffer(gatewayResponse.getFutureResponse()
                    .getResponseBodyAsByteBuffer());
        }
        else if(gatewayResponse.getContent() != null) {     // 存在响应内容: 文本
            content = Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes());
        }
        else {  // 空
            content = Unpooled.wrappedBuffer(BasicConstants.BLANK_SEPARATOR_1.getBytes());
        }

        // 响应内容: 文本
        if(Objects.isNull(gatewayResponse.getFutureResponse())) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    gatewayResponse.getHttpStatus(),
                    content);
            // 常规响应头
            httpResponse.headers().add(gatewayResponse.getHeaders());
            // 额外响应头
            httpResponse.headers().add(gatewayResponse.getExtraHeaders());
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            return httpResponse;
        } else {    // 响应对象
            gatewayResponse.getFutureResponse().getHeaders().add(gatewayResponse.getExtraHeaders());

            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(gatewayResponse.getFutureResponse().getStatusCode()),
                    content);
            httpResponse.headers().add(gatewayResponse.getFutureResponse().getHeaders());
            return httpResponse;
        }
    }

    /**
     * 写回响应信息
     * @param gatewayContext
     */
    public static void writeResponse(Context gatewayContext) {
        // 释放请求资源
        gatewayContext.releaseRequest();

        // 设置SS: 网关作为服务端写回响应的时间
        gatewayContext.setSSTime(TimeUtil.currentTimeMillis());

        if (gatewayContext.isWritten()) {
            // 1. 构建响应对象，并写回数据
            FullHttpResponse response = ResponseHelper.getHttpResponse(gatewayContext, (GatewayResponse) gatewayContext.getResponse());

            // 长连接
            if (gatewayContext.isKeepAlive()) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                gatewayContext.getNettyCtx().writeAndFlush(response);
            } else {
                gatewayContext.getNettyCtx().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }

            // 2. 设置写回成功状态为: COMPLETED
            gatewayContext.completed();
        } else if (gatewayContext.isCompleted()) {
            gatewayContext.invokeCompletedCallback();
        }
    }
}
