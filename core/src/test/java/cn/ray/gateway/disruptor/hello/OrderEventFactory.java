package cn.ray.gateway.disruptor.hello;

import com.lmax.disruptor.EventFactory;

/**
 * @author Ray
 * @date 2023/10/15 07:18
 * @description 事件工厂类，用于disruptor初始化时填充ringBuffer
 */
public class OrderEventFactory implements EventFactory<OrderEvent> {
    @Override
    public OrderEvent newInstance() {
        return new OrderEvent();
    }
}
