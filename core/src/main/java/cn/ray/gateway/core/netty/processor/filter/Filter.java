package cn.ray.gateway.core.netty.processor.filter;

import java.lang.annotation.*;

/**
 * @author Ray
 * @date 2023/11/22 13:38
 * @description 过滤器注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter {

    /**
     * 过滤器的唯一ID, 必须
     * @return
     */
    String id();

    /**
     * 过滤器名称
     * @return
     */
    String name() default "";

    /**
     * 过滤器类型, 必须
     * @return
     */
    ProcessorFilterType type();

    /**
     * 过滤器排序(优先级), 从小到大依次执行过滤器
     * @return
     */
    int order() default 0;
}
