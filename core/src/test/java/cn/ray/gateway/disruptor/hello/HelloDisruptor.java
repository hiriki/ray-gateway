package cn.ray.gateway.disruptor.hello;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * @author Ray
 * @date 2023/10/15 07:15
 * @description
 */
public class HelloDisruptor {

    public static void main(String[] args) {
        int ringBufferSize = 1024 * 1024;
        OrderEventFactory orderEventFactory = new OrderEventFactory();
        Disruptor<OrderEvent> disruptor = new Disruptor<>(
                orderEventFactory,
                ringBufferSize,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("disruptor : ");
                        return thread;
                    }
                },
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(new OrderEventHandler());

        disruptor.start();

        OrderEventProducer producer = new OrderEventProducer(disruptor.getRingBuffer());

        for (int i = 0; i < 100; i++) {
            producer.produce(i);
        }

        disruptor.shutdown();
    }
}
