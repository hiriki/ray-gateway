package cn.ray.gateway.core.helper;

import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.constants.GatewayConstants;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.GatewayResponseException;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.context.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ray
 * @date 2023/11/13 08:21
 * @description 解析请求信息，构建上下文对象
 */
public class RequestHelper {

    /**
     * 解析FullHttpRequest 构建GatewayContext
     * @param fullHttpRequest
     * @param ctx
     * @return
     */
    public static GatewayContext doContext(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {

        //	1. 	构建请求对象GatewayRequest
        GatewayRequest gatewayRequest = doRequest(fullHttpRequest, ctx);

        //  2.  根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)

        return null;
    }

    /**
     * 构建GatewayRequest请求对象
     * @param fullHttpRequest
     * @param ctx
     * @return GatewayRequest
     */
    private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullHttpRequest.headers();

        //	从header头获取必须要传入的关键属性 uniqueId
        String uniqueId = headers.get(GatewayConstants.UNIQUE_ID);

        if (StringUtils.isBlank(uniqueId)) {
            throw new GatewayResponseException(ResponseCode.REQUEST_PARSE_ERROR_NO_UNIQUEID);
        }

        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod httpMethod = fullHttpRequest.method();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(fullHttpRequest, ctx);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

        return new GatewayRequest(
                uniqueId,
                charset,
                clientIp,
                host,
                uri,
                httpMethod,
                contentType,
                headers,
                fullHttpRequest
        );
    }

    /**
     * 获取客户端ip
     * @param fullHttpRequest
     * @param ctx
     * @return clientIp
     */
    private static String getClientIp(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        //  X-Forwarded-For（XFF）是用来识别通过HTTP代理或负载均衡方式连接到Web服务器的客户端最原始的IP地址的HTTP头字段
        //  一般格式如下:
        //  X-Forwarded-For: client1, proxy1, proxy2
        //  其中的值通过一个 逗号+空格 把多个IP地址区分开, 最左边（client1）是最原始客户端的IP地址
        //  代理服务器每成功收到一个请求，就把请求来源IP地址添加到右边
        //  在上面这个例子中，这个请求成功通过了三台代理服务器：proxy1、proxy2和proxy3
        //  请求由client1发出，到达了proxy3（proxy3可能是请求的终点）
        //  请求刚从client1中发出时，XFF是空的，请求被发往proxy1
        //  通过proxy1的时候，client1被添加到XFF中，之后请求被发往proxy2
        //  通过proxy2的时候，proxy1被添加到XFF中，之后请求被发往proxy3
        //  通过proxy3时，proxy2被添加到XFF中，之后请求的的去向不明，如果proxy3不是请求终点，请求会被继续转发。
        String xForwardedValue = fullHttpRequest.headers().get(BasicConstants.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;
        if(StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            //  isEmpty: str==null 或 str.length()==0
            //  isBlank: 某字符串是否为空或长度为0或由空白符(whitespace)构成
            if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }

        if (clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }

        return clientIp;
    }
}
