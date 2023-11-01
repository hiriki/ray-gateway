package cn.ray.gateway.common.concurrent.queue.flusher;

/**
 * @author Ray
 * @date 2023/10/31 17:16
 * @description
 */
public interface Flusher<E> {

    /**
     * 添加事件
     * @param event 事件
     */
    void add(E event);

    /**
     * 添加多个事件
     * @param events 事件
     */
    void add(E... events);

    /**
     * 尝试添加一个事件, 如果添加成功返回true 失败返回false
     * @param event 事件
     * @return 是否添加成功
     */
    boolean tryAdd(E event);

    /**
     * 尝试添加多个事件, 如果添加成功返回true 失败返回false
     * @param events 事件
     * @return 是否添加成功
     */
    boolean tryAdd(E... events);

    /**
     * 是否关闭
     * @return
     */
    boolean isShutDown();

    /**
     * 开启
     */
    void start();

    /**
     * 关闭
     */
    void shutdown();
}
