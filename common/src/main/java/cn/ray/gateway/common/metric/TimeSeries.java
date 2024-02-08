package cn.ray.gateway.common.metric;

import lombok.Data;

/**
 * @author Ray
 * @date 2024/2/8 21:28
 * @description
 */
@Data
public abstract class TimeSeries {

    /**
     * 消息投递目标地址: kafka topic
     */
    protected transient String destination;

    /**
     * 时序数据时间戳
     */
    protected long timestamp;

    /**
     * 消息是否分区，不需要序列化
     */
    protected transient boolean enablePartitionHash;
}
