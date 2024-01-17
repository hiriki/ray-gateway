package cn.ray.gateway.common.enums;

/**
 * @author Ray
 * @date 2024/1/17 15:56
 * @description 负载均衡策略枚举
 */
public enum LoadBalanceStrategy {

    RANDOM("RANDOM","随机负载均衡策略"),

    ROUND_ROBIN("ROUND_ROBIN","轮询负载均衡策略");

    private String val;

    private String desc;

    LoadBalanceStrategy(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public String getVal() {
        return val;
    }

    public String getDesc() {
        return desc;
    }
}
