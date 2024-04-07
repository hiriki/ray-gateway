package cn.ray.gateway.client;

/**
 * @author Ray
 * @date 2023/12/3 22:50
 * @description 表示注册服务的协议枚举类
 */
public enum GatewayProtocol {

    HTTP("http", "http协议"),
    DUBBO("dubbo", "dubbo协议");

    private String code;

    private String desc;

    GatewayProtocol(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
