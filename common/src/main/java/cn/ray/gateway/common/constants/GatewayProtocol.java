package cn.ray.gateway.common.constants;

/**
 * @author Ray
 * @date 2023/11/20 22:53
 * @description 协议定义类
 */
public interface GatewayProtocol {

    String HTTP = "http";

    String DUBBO = "dubbo";

    static boolean isHttp(String protocol) {
        return HTTP.equals(protocol);
    }

    static boolean isDubbo(String protocol) {
        return DUBBO.equals(protocol);
    }
}
