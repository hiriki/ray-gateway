package cn.ray.gateway.core.netty.processor.filter;

import lombok.Data;

/**
 * @author Ray
 * @date 2023/11/30 19:52
 * @description 所有的过滤器配置实现类的 Base 类, 定义一些共性配置信息
 */
@Data
public class FilterConfig {

    /**
     * 	是否打印日志
     */
    private boolean loggable = false;


}
