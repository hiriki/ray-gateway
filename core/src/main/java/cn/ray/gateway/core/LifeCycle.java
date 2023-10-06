package cn.ray.gateway.core;

/**
 * @author Ray
 * @date 2023/10/6 08:11
 * @description 生命周期管理接口
 */
public interface LifeCycle {

    /**
     * 初始化
     */
    void init();

    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void shutdown();

}
