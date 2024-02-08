package cn.ray.gateway.common.metric;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ray
 * @date 2024/2/8 21:30
 * @description 一元数据指标结构
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class Metric extends TimeSeries implements Serializable {

    private static final long serialVersionUID = 4754755625101487737L;

    /**
     * 指标名称
     */
    @JSONField(name = "name")
    protected String name;

    /**
     * 指标取值
     */
    @JSONField(name = "value")
    protected Number value;

    /**
     * 标签集合
     */
    protected Map<String, String> tags = new HashMap<>();

    /**
     * 	{
     * 		metricName: key
     * 		metricValue: Long
     * 		timestamp: 时间戳
     * 		tags:{ k1:v1 , k2:v2}
     * 	}
     */
    private Metric() {}

    /**
     * 创建指标
     * @param name              指标名称
     * @param value             指标取值
     * @param timestamp         时间戳
     * @param tags              标签集合
     * @param destination       sink目标源
     * @param enablePartition   是否分区
     * @return                  Metric
     */
    public static Metric create(String name, Number value, long timestamp, Map<String, String> tags, String destination, boolean enablePartition) {
        Metric metric = new Metric();
        metric.setName(name);
        metric.setValue(value);
        metric.setTimestamp(timestamp);
        metric.getTags().putAll(tags);
        metric.setDestination(destination);
        metric.setEnablePartitionHash(enablePartition);
        return metric;
    }
}
