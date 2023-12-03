package cn.ray.gateway.client;

import java.lang.annotation.*;

/**
 * @author Ray
 * @date 2023/12/3 22:58
 * @description 方法级别的调用声明
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GatewayInvoker {

    /**
     * 访问路径
     * @return
     */
    String path();
}
