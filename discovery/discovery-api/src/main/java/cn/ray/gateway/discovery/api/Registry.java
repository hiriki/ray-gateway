package cn.ray.gateway.discovery.api;

import cn.ray.gateway.common.utils.Pair;

import java.util.List;

/**
 * @author Ray
 * @date 2023/12/11 08:25
 * @description 注册接口, 包括各种数据相关操作
 */
public interface Registry {

    /**
     * /services: 存储所有的服务定义信息的: ServiceDefinition (永久存储)
     */
    String SERVICE_PREFIX = "/services";

    /**
     * /instances: 存储所有的服务实例信息: ServiceInstance (加载时存储)
     */
    String INSTANCE_PREFIX = "/instances";

    /**
     * /rules: 存储所有的规则信息: Rule (永久存储)
     */
    String RULE_PREFIX = "/rules";

    /**
     * /gateway: 这个是要存储所有的网关本身自注册信息的: core(网关服务本身，加载时存储)
     */
    String GATEWAY_PREFIX = "/gateway";

    String PATH = "/";

    /**
     * 注册一个路径如果不存在
     * @param path key
     * @param value value
     * @param isPermanent 是否永久节点
     * @throws Exception
     */
    void registerPathIfNotExists(String path, String value, boolean isPermanent) throws Exception;

    /**
     * 根据key, 判断键值对是否存在
     * @param key key
     * @return
     * @throws Exception
     */
    boolean isExist(String key) throws Exception;

    /**
     * 注册一个永久节点
     * @param key
     * @param value
     * @throws Exception
     */
    void registerPermanentNode(String key, String value) throws Exception;

    /**
     * 注册一个临时节点
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    long registerTemporaryNode(String key, String value) throws Exception;

    /**
     * 通过一个前缀路径，获取对应的键值对集合
     * @param prefix
     * @return
     * @throws Exception
     */
    List<Pair<String, String>> getListByPrefixPath(String prefix) throws Exception;

    /**
     * 通过一个key查询对应键值对
     * @param key
     * @return
     * @throws Exception
     */
    Pair<String, String> getByKey(String key) throws Exception;

    /**
     * 根据key删除
     * @param key
     */
    void deleteByKey(String key);
}
