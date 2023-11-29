package cn.ray.gateway.core.netty.processor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ray
 * @date 2023/11/29 23:55
 * @description 全局缓存管理器
 */
public class DefaultCacheManager {

    private DefaultCacheManager() {}

    private static class SingletonHolder {
        private static final DefaultCacheManager INSTANCE = new DefaultCacheManager();
    }

    public static DefaultCacheManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static final String FILTER_CONFIG_CACHE_ID = "filterConfigCache";

    //	双层缓存: 之后可以将其他需要缓存的地方也定义一个ID, 作为 key, 新建一个 caffeine 对象作为 value 进行缓存
    private final ConcurrentHashMap<String, Cache<String, ?>> cacheMap = new ConcurrentHashMap<>();

    /**
     * 根据一个 全局缓存ID, 创建下层 Caffeine 缓存
     * @param cacheId
     * @return
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    public <V> Cache<String, V> create(String cacheId) {
        Cache<String, V> cache = Caffeine.newBuilder().build();
        cacheMap.put(cacheId, cache);
        return (Cache<String, V>) cacheMap.get(cacheId);
    }

    /**
     * 根据一个 全局缓存ID 以及 真实的下层缓存 key, 删除对应的缓存对象
     * @param cacheId
     * @param key
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    public <V> void remove(String cacheId, String key) {
        Cache<String, V>  cache = (Cache<String, V>) cacheMap.get(cacheId);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * 根据一个 全局缓存ID, 删除下层 Caffeine 缓存
     * @param cacheId
     * @param <V>
     */
    @SuppressWarnings("unchecked")
    public <V> void remove(String cacheId) {
        Cache<String, V>  cache = (Cache<String, V>) cacheMap.get(cacheId);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        cacheMap.values().forEach(cache -> cache.invalidateAll());
    }
}
