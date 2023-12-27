package cn.ray.gateway.discovery.api;

/**
 * @author Ray
 * @date 2023/12/11 09:29
 * @description 注册服务接口, 继承 Registry, 同时定义注册中心相关操作, 位于最上层给外部提供服务
 */
public interface RegistryService extends Registry {

    /**
     * 添加监听事件
     * @param superPath 父节点目录
     * @param notify 监听函数
     */
    void addWatchers(String superPath, Notify notify);

    /**
     * 初始化注册服务
     * @param registryAddress 注册服务地址
     */
    void initialized(String registryAddress);

    /**
     * 关闭服务
     */
    void close();
}
