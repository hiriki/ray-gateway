package cn.ray.gateway.core.netty.processor;

import cn.ray.gateway.core.context.HttpRequestWrapper;

/**
 * @author Ray
 * @date 2023/10/10 22:54
 * @description 处理Netty核心逻辑的执行器接口定义
 */
public interface NettyProcessor {

    /**
     * 核心执行方法
     * @param httpRequestWrapper
     */
    void process(HttpRequestWrapper httpRequestWrapper) throws Exception;

    /**
     * 执行器启动方法
     */
    void start();

    /**
     * 执行器资源释放/关闭方法
     */
    void shutdown();
}
