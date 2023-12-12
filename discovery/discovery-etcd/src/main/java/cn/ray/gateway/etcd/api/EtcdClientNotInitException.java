package cn.ray.gateway.etcd.api;

/**
 * @author Ray
 * @date 2023/12/12 19:06
 * @description
 */
public class EtcdClientNotInitException extends RuntimeException {

    private static final long serialVersionUID = -617743243793838282L;

    public EtcdClientNotInitException() {
        super();
    }

    public EtcdClientNotInitException(String message) {
        super(message);
    }

}
