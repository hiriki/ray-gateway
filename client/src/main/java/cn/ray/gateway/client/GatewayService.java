package cn.ray.gateway.client;

import java.lang.annotation.*;

/**
 * @author Ray
 * @date 2023/12/3 22:43
 * @description 服务定义注解, 作用于类级别
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayService {

    /**
     * 服务的唯一ID
     * @return
     */
    String serviceId();

    /**
     * 对应服务的版本号
     * @return
     */
    String version() default "1.0.0";

    /**
     * 协议类型
     * @return
     */
    GatewayProtocol protocol();

    /**
     * ANT路径匹配表达式配置: 比如 /user/**
     * @return
     */
    String patternPath();
}
