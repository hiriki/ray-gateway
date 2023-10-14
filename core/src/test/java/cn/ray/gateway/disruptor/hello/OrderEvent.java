package cn.ray.gateway.disruptor.hello;

/**
 * @author Ray
 * @date 2023/10/15 07:16
 * @description ringBuffer存储的数据模型
 */
public class OrderEvent {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
