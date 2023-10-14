package cn.ray.gateway.disruptor.hello;

import com.lmax.disruptor.EventHandler;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author Ray
 * @date 2023/10/15 07:24
 * @description 事件处理器，消费者
 */
public class OrderEventHandler implements EventHandler<OrderEvent> {
    @Override
    public void onEvent(OrderEvent orderEvent, long l, boolean b) throws Exception {
        Thread.sleep(RandomUtils.nextInt(1,100));
        System.out.println("消费者消费:" + orderEvent.getValue());
    }
}
