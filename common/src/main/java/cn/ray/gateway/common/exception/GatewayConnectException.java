package cn.ray.gateway.common.exception;

import cn.ray.gateway.common.enums.ResponseCode;
import lombok.Getter;

/**
 * @author Ray
 * @date 2024/1/21 03:38
 * @description 网关连接异常类
 */
public class GatewayConnectException extends GatewayBaseException {

    private static final long serialVersionUID = -8503239867913964958L;

    @Getter
    private final String uniqueId;

    @Getter
    private final String requestUrl;

    public GatewayConnectException(String uniqueId, String requestUrl) {
        this.uniqueId = uniqueId;
        this.requestUrl = requestUrl;
    }

    public GatewayConnectException(Throwable throwable, String uniqueId, String requestUrl, ResponseCode code) {
        super(code.getMessage(), throwable, code);
        this.uniqueId = uniqueId;
        this.requestUrl = requestUrl;
    }
}
