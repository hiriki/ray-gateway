package cn.ray.gateway.core.context;

import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.utils.JSONUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

/**
 * @author Ray
 * @date 2023/11/8 20:39
 * @description 网关响应封装类
 */
@Data
public class GatewayResponse {

    /**
     * 响应头
     */
    private HttpHeaders headers = new DefaultHttpHeaders();

    /**
     * 额外响应头
     */
    private final HttpHeaders extraHeaders = new DefaultHttpHeaders();

    /**
     * 响应内容
     */
    private String content;

    /**
     * 响应状态码
     */
    private HttpResponseStatus httpStatus;

    /**
     * 响应对象
     */
    private Response futureResponse;

    private GatewayResponse() {
    }

    /**
     * 添加响应头信息
     * @param key
     * @param val
     */
    public void addHeader(CharSequence key, CharSequence val) {
        headers.add(key, val);
    }

    /**
     * 构建网关响应对象
     * @param futureResponse org.asynchttpclient.Response
     * @return GatewayResponse
     */
    public static GatewayResponse buildGatewayResponse(Response futureResponse) {
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setFutureResponse(futureResponse);
        gatewayResponse.setHttpStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return gatewayResponse;
    }

    /**
     * 根据自定义响应枚举返回一个json类型的响应信息
     * @param responseCode
     * @param args
     * @return
     */
    public static GatewayResponse buildGatewayResponse(ResponseCode responseCode, Object... args) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.CODE, responseCode.getCode());
        objectNode.put(JSONUtil.STATUS, responseCode.getStatus().code());
        objectNode.put(JSONUtil.DATA, responseCode.getMessage());
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setHttpStatus(responseCode.getStatus());
        gatewayResponse.addHeader(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        gatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
        return gatewayResponse;
    }

    /**
     * 返回一个json类型的响应信息: 响应成功
     * @param data
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Object data) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, data);
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setHttpStatus(ResponseCode.SUCCESS.getStatus());
        gatewayResponse.addHeader(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        gatewayResponse.setContent(JSONUtil.toJSONString(objectNode));
        return gatewayResponse;
    }

}
