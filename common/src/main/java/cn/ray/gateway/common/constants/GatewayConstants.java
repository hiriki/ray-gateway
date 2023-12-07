package cn.ray.gateway.common.constants;

/**
 * @author Ray
 * @date 2023/11/13 08:47
 * @description 网关常量类: 与业务相关
 */
public interface GatewayConstants {

    String GATEWAY =  "gateway";

    String UNIQUE_ID = "uniqueId";

    String DEFAULT_VERSION = "1.0.0";

    String PROTOCOL_KEY = "protocol";

    /**
     * 	默认的实例权重为100
     */
    int DEFAULT_WEIGHT = 100;

    /**
     * 	请求超时时间默认为20s
     */
    int DEFAULT_REQUEST_TIMEOUT = 20000;
}
