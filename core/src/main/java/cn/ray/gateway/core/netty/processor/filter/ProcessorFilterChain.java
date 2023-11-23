package cn.ray.gateway.core.netty.processor.filter;

/**
 * @author Ray
 * @date 2023/11/23 17:52
 * @description 过滤器链的抽象类, 定义一些关于链表的操作方法
 */
public abstract class ProcessorFilterChain<T> extends AbstractLinkedProcessorFilter<T> {

    /**
     * 在链表的头部添加元素
     * @param filter
     */
    public abstract void addFirst(AbstractLinkedProcessorFilter<T> filter);

    /**
     * 在链表的尾部添加元素
     * @param filter
     */
    public abstract void addLast(AbstractLinkedProcessorFilter<T> filter);
}
