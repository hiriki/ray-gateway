package cn.ray.gateway.core.netty.processor.filter;

/**
 * @author Ray
 * @date 2023/11/22 13:26
 * @description 执行过滤器操作的接口定义
 */
public interface ProcessorFilter<T> {

    /**
     * 过滤器是否执行的校验方法
     * @param t
     * @return
     * @throws Throwable
     */
    boolean check(T t) throws Throwable;

    /**
     * 真正执行过滤器的方法
     * @param t
     * @param args
     * @throws Throwable
     */
    void entry(T t, Object... args) throws Throwable;

    /**
     * 触发下一个过滤器执行
     * @param t
     * @param args
     * @throws Throwable
     */
    void fireNext(T t, Object... args) throws Throwable;

    /**
     * 对象传输的方法
     * @param t
     * @param args
     * @throws Throwable
     */
    void transformEntry(T t, Object... args) throws Throwable;

    /**
     * 过滤器初始化的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void init() throws Exception {}

    /**
     * 过滤器销毁的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void destroy() throws Exception {}

    /**
     * 过滤器刷新的方法，如果子类有需求则进行覆盖
     * @throws Exception
     */
    default void refresh() throws Exception {}
}
