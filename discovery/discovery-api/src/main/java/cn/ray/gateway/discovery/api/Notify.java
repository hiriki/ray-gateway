package cn.ray.gateway.discovery.api;

/**
 * @author Ray
 * @date 2023/12/11 09:26
 * @description 监听服务接口, 定义发生某种操作时需要执行的逻辑
 */
public interface Notify {

    /**
     * 添加或者更新
     * @param key
     * @param value
     * @throws Exception
     */
    void put(String key, String value) throws Exception;

    /**
     * 删除
     * @param key
     * @throws Exception
     */
    void delete(String key) throws Exception;
}
