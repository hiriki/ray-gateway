package cn.ray.gateway.core.netty.processor.filter;

import cn.ray.gateway.core.context.Context;

/**
 * @author Ray
 * @date 2023/11/22 22:54
 * @description 过滤器抽象类, 定义一些基础逻辑操作，相当于链表元素
 */
public abstract class AbstractLinkedProcessorFilter<T> implements ProcessorFilter<Context> {

    /**
     * 串行化, next 指向下一个过滤器
     */
    protected AbstractLinkedProcessorFilter<T> next = null;

    @Override
    public void fireNext(Context context, Object... args) throws Throwable {
        if (this.next != null) {     // 过滤器存在且不为最后一个过滤器
            if (!this.next.check(context)) {     // 过滤器不需要执行
                this.next.fireNext(context, args);   // 递归判断下一节点
            } else {
                this.next.transformEntry(context, args);
            }
        }
    }

    @Override
    public void transformEntry(Context context, Object... args) throws Throwable {
        // 子类调用：这里就是真正执行下一个节点(元素)的操作
        entry(context, args);
    }

    public AbstractLinkedProcessorFilter<T> getNext() {
        return next;
    }

    public void setNext(AbstractLinkedProcessorFilter<T> next) {
        this.next = next;
    }
}
