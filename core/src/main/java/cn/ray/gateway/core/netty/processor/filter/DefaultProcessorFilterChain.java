package cn.ray.gateway.core.netty.processor.filter;

import cn.ray.gateway.core.context.Context;

/**
 * @author Ray
 * @date 2023/11/23 17:57
 * @description 默认过滤器链实现类
 */
public class DefaultProcessorFilterChain extends ProcessorFilterChain<Context> {

    /**
     * 虚拟头节点
     */
    AbstractLinkedProcessorFilter<Context> first = new AbstractLinkedProcessorFilter<Context>() {
        @Override
        public boolean check(Context context) throws Throwable {
            // 虚拟头节点放行,必须执行
            return true;
        }

        @Override
        public void entry(Context context, Object... args) throws Throwable {
            // 虚拟头节点放行,直接转到下一个过滤器
            super.fireNext(context, args);
        }
    };

    /**
     * 尾节点
     */
    AbstractLinkedProcessorFilter<Context> end = first;

    @Override
    public boolean check(Context context) throws Throwable {
        return true;
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        // 虚拟头节点执行 transformEntry -> first.entry -> fireNext
        first.transformEntry(context, args);
    }

    @Override
    public void addFirst(AbstractLinkedProcessorFilter<Context> filter) {
        filter.setNext(first.getNext());
        first.setNext(filter);
        if(end == first) {
            end = filter;
        }
    }

    @Override
    public void addLast(AbstractLinkedProcessorFilter<Context> filter) {
        end.setNext(filter);
        end = filter;
    }

    /**
     * 获取串行化过滤器中需要执行的
     * @return
     */
    @Override
    public AbstractLinkedProcessorFilter<Context> getNext() {
        return first.getNext();
    }

    /**
     * 向串行化过滤器尾部添加
     * @param filter
     */
    @Override
    public void setNext(AbstractLinkedProcessorFilter<Context> filter) {
        addLast(filter);
    }
}
