package cn.ray.gateway.core.helper;

import cn.ray.gateway.common.enums.ResponseCode;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

/**
 * @author Ray
 * @date 2023/11/3 08:34
 * @description 构造xingying
 */
public class ResponseHelper {

    public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
        //	TODO: 目前硬编码
        String errorContent = "响应内部错误";
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(errorContent.getBytes()));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, errorContent.length());
        return httpResponse;
    }

}
