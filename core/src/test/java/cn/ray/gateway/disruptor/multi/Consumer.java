package cn.ray.gateway.disruptor.multi;

import com.lmax.disruptor.WorkHandler;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ray
 * @date 2023/10/31 00:57
 * @description
 */
public class Consumer implements WorkHandler<Order> {

    private String consumerId;

    private static AtomicInteger count = new AtomicInteger(0);

    private Random random = new Random();

    public Consumer(String consumerId) {
        this.consumerId = consumerId;
    }

    @Override
    public void onEvent(Order order) throws Exception {
        Thread.sleep(random.nextInt(5));
        System.err.println("当前消费者: " + this.consumerId + ", 消费信息ID: " + order.getId());
        count.incrementAndGet();
    }

    public Integer getCount() {
        return count.get();
    }
}
