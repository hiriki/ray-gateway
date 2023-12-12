package cn.ray.gateway.etcd.api;

import io.etcd.jetcd.KeyValue;

/**
 * @author Ray
 * @date 2023/12/12 19:00
 * @description
 */
public class EtcdChangedEvent {

    public static enum Type {
        PUT,
        DELETE,
        UNRECOGNIZED;
    }

    private KeyValue prevKeyValue;

    private KeyValue curtkeyValue;

    private Type type;

    public EtcdChangedEvent(KeyValue prevKeyValue, KeyValue curtkeyValue, Type type) {
        this.prevKeyValue = prevKeyValue;
        this.curtkeyValue = curtkeyValue;
        this.type = type;
    }

    public KeyValue getCurtkeyValue() {
        return curtkeyValue;
    }

    public KeyValue getPrevKeyValue() {
        return prevKeyValue;
    }

    public Type getType() {
        return type;
    }

}
