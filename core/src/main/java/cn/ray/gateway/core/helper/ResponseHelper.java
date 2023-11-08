package cn.ray.gateway.core.helper;

import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.core.context.GatewayResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

/**
 * @author Ray
 * @date 2023/11/3 08:34
 * @description 构造xingying
 */
public class ResponseHelper {

    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponse(responseCode);
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(gatewayResponse.getContent().getBytes()));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        return httpResponse;
    }

}
