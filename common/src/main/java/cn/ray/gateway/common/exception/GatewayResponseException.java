package cn.ray.gateway.common.exception;

import cn.ray.gateway.common.enums.ResponseCode;

/**
 * @author Ray
 * @date 2023/10/6 06:36
 * @description 所有的响应异常基础定义
 */
public class GatewayResponseException extends GatewayBaseException {

    private static final long serialVersionUID = -5658789202509039759L;

    public GatewayResponseException() {
        this(ResponseCode.INTERNAL_ERROR);
    }

    public GatewayResponseException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public GatewayResponseException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.code = code;
    }

}
