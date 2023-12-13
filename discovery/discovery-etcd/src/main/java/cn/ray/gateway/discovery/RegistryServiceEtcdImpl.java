package cn.ray.gateway.discovery;

import cn.ray.gateway.common.utils.Pair;
import cn.ray.gateway.discovery.api.Notify;
import cn.ray.gateway.discovery.api.Registry;
import cn.ray.gateway.discovery.api.RegistryService;
import cn.ray.gateway.etcd.api.EtcdChangedEvent;
import cn.ray.gateway.etcd.api.EtcdClient;
import cn.ray.gateway.etcd.api.HeartBeatLeaseTimeoutListener;
import cn.ray.gateway.etcd.api.WatcherListener;
import cn.ray.gateway.etcd.core.EtcdClientImpl;
import io.etcd.jetcd.KeyValue;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ray
 * @date 2023/12/13 15:36
 * @description etcd 注册中心服务实现
 */
@Slf4j
public class RegistryServiceEtcdImpl implements RegistryService {

    private EtcdClient etcdClient;

    private Map<String, String> cachedMap = new HashMap<>();

    @Override
    public void initialized(String registryAddress) {
        //	初始化etcd客户端对象
        this.etcdClient = new EtcdClientImpl(registryAddress, true);
        //	添加异常的过期处理监听
        etcdClient.addHeartBeatLeaseTimeoutNotifyListener(new HeartBeatLeaseTimeoutListener() {
            @Override
            public void timeoutNotify() {
                cachedMap.forEach((key, value) ->{
                    try {
                        registerTemporaryNode(key, value);
                    } catch (Exception e) {
                        //	ignore
                        log.error("#RegistryServiceEtcdImpl.initialized# HeartBeatLeaseTimeoutListener: timeoutNotify is error", e);
                    }
                });
            }
        });
    }

    /**
     * 服务变更 添加监听
     * @param superPath 父节点目录
     * @param notify 监听函数
     * @see cn.ray.gateway.discovery.api.RegistryService#addWatcher(java.lang.String, cn.ray.gateway.discovery.api.Notify)
     */
    @Override
    public void addWatcher(String superPath, Notify notify) {
        this.etcdClient.addWatcherListener(superPath + Registry.SERVICE_PREFIX, true, new InnerWatcherListener(notify));
        this.etcdClient.addWatcherListener(superPath + Registry.INSTANCE_PREFIX, true, new InnerWatcherListener(notify));
        this.etcdClient.addWatcherListener(superPath + Registry.RULE_PREFIX, true, new InnerWatcherListener(notify));
        this.etcdClient.addWatcherListener(superPath + Registry.GATEWAY_PREFIX, true, new InnerWatcherListener(notify));
    }

    /**
     * 面向接口编程: 通过实现 WatcherListener 接口, 包装api中的notify, 外部只需传入notify的实现而无需关注其内部逻辑处理
     * Watch.Listener(etcd) ==> this.watcherListener.watcherKeyChanged(etcd.api) ==> notify(discovery)
     */
    static class InnerWatcherListener implements WatcherListener {

        private final Notify notify;

        public InnerWatcherListener(Notify notify) {
            this.notify = notify;
        }

        @Override
        public void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception {
            EtcdChangedEvent.Type eventType = event.getType();
            KeyValue curtkeyValue = event.getCurtkeyValue();

            switch (eventType) {
                case PUT:
                    notify.put(curtkeyValue.getKey().toString(Charset.defaultCharset()),
                           curtkeyValue.getValue().toString(Charset.defaultCharset()));
                    break;
                case DELETE:
                    notify.delete(curtkeyValue.getKey().toString(Charset.defaultCharset()));
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void registerPathIfNotExists(String path, String value, boolean isPermanent) throws Exception {
        if (!isExist(path)) {
            if (isPermanent) {
                registerPermanentNode(path, value);
            } else {
                registerTemporaryNode(path, value);
            }
        }
    }

    @Override
    public boolean isExist(String key) throws Exception {
        KeyValue keyValue = this.etcdClient.getKey(key);
        return keyValue != null;
    }

    @Override
    public void registerPermanentNode(String key, String value) throws Exception {
        this.etcdClient.putKey(key, value);
    }

    @Override
    public long registerTemporaryNode(String key, String value) throws Exception {
        long heartBeatLeaseId = this.etcdClient.getHeartBeatLeaseId();
        cachedMap.put(key, value);
        return this.etcdClient.putKeyWithLeaseId(key, value, heartBeatLeaseId);
    }

    @Override
    public List<Pair<String, String>> getListByPrefixPath(String prefix) throws Exception {
        List<KeyValue> keyValues = this.etcdClient.getKeyWithPrefix(prefix);
        List<Pair<String, String>> res = new ArrayList<>();

        for (KeyValue kv : keyValues) {
            res.add(new Pair<>(kv.getKey().toString(Charset.defaultCharset()),
                    kv.getValue().toString(Charset.defaultCharset())));
        }
        return res;
    }

    @Override
    public Pair<String, String> getByKey(String key) throws Exception {
        KeyValue keyValue = this.etcdClient.getKey(key);
        return new Pair<>(keyValue.getKey().toString(Charset.defaultCharset()),
                keyValue.getValue().toString(Charset.defaultCharset()));
    }

    @Override
    public void deleteByKey(String key) {
        this.etcdClient.deleteKey(key);
    }

    @Override
    public void close() {
        if (this.etcdClient != null) {
            this.etcdClient.close();
        }
    }
}
