package cn.ray.gateway.etcd.api;

/**
 * @author Ray
 * @date 2023/12/12 19:04
 * @description
 */
public interface WatcherListener {

    void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception;

}
