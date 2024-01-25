package cn.ray.gateway.core.context;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ray
 * @date 2024/1/25 09:34
 * @description
 */
@Getter
@Setter
public class DubboRequest {

    /**
     * dubbo服务的注册地址
     */
    private String registriesStr;

    /**
     * dubbo服务的接口全类名
     */
    private String interfaceClass;

    /**
     * dubbo服务的方法名
     */
    private String methodName;

    /**
     * dubbo服务的方法参数类型
     */
    private String[] parameterTypes;

    /**
     * dubbo服务的方法参数值
     */
    private Object[] args;

    /**
     * dubbo服务的调用超时时间
     */
    private int timeout;

    /**
     * dubbo服务的版本号
     */
    private String version;
}
