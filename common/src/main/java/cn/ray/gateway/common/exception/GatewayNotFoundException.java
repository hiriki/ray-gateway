package cn.ray.gateway.common.exception;

import cn.ray.gateway.common.enums.ResponseCode;

/**
 * @author Ray
 * @date 2023/11/20 09:19
 * @description 服务信息未找到异常定义：比如服务定义、实例等信息未找到均会抛出此异常
 */
public class GatewayNotFoundException extends GatewayBaseException {
    private static final long serialVersionUID = -5534700534739261761L;

    public GatewayNotFoundException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public GatewayNotFoundException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
    }

}
