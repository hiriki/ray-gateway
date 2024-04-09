package cn.ray.gateway.rolling;

import cn.ray.gateway.core.rolling.RollingNumber;
import cn.ray.gateway.core.rolling.RollingNumberEvent;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author Ray
 * @date 2024/4/9 02:08
 * @description
 */
public class RollingTest {

    public static final Integer windowSize = 10000;

    public static final Integer bucketsSize = 10;

    public static void main(String[] args) throws InterruptedException {

        //	默认是10个桶，所以每个桶是1秒
        RollingNumber rollingNumber = new RollingNumber(windowSize, bucketsSize, "hello:1.0.0", null);

        while (true) {
            rollingNumber.increment(RollingNumberEvent.SUCCESS);// 事件 + 增量
            Thread.sleep(RandomUtils.nextInt(1, 10));
        }
    }

}
