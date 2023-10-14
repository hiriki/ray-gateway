package cn.ray.gateway.disruptor.hello;

import com.lmax.disruptor.RingBuffer;

/**
 * @author Ray
 * @date 2023/10/15 07:29
 * @description 生产者
 */
public class OrderEventProducer {

    private RingBuffer<OrderEvent> ringBuffer;

    public OrderEventProducer(RingBuffer<OrderEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void produce(int value) {
        // 先获取下一个可用的序号
        long sequence = ringBuffer.next();
        try {
            //	通过可用的序号获取对应下标的数据OrderEvent
            OrderEvent event = ringBuffer.get(sequence);
            //	重新设置内容
            event.setValue(value);
        } finally {
            //	publish
            ringBuffer.publish(sequence);
        }
    }

}
