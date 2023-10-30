package cn.ray.gateway.disruptor.multi;

import com.lmax.disruptor.RingBuffer;

/**
 * @author Ray
 * @date 2023/10/31 01:02
 * @description
 */
public class Producer {

    private RingBuffer<Order> ringBuffer;

    public Producer() {
    }

    public Producer(RingBuffer<Order> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void produce(String id) {
        long next = ringBuffer.next();
        try {
            Order order = ringBuffer.get(next);
            order.setId(id);
        } finally {
            ringBuffer.publish(next);
        }
    }
}
