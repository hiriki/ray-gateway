package cn.ray.gateway.etcd.api;

/**
 * @author Ray
 * @date 2023/12/12 19:03
 * @description
 */
public interface HeartBeatLeaseTimeoutListener {

    void timeoutNotify();

}
