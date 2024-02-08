package cn.ray.gateway.core.plugin.metric.kafka;

import cn.ray.gateway.common.metric.MetricClientCollector;
import cn.ray.gateway.common.metric.MetricException;
import cn.ray.gateway.common.metric.TimeSeries;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import org.apache.kafka.clients.producer.*;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Ray
 * @date 2024/2/8 21:58
 * @description
 */
public class MetricKafkaClientCollector implements MetricClientCollector {

    /**
     * 每个发送批次大小为 16K
     */
    private final int batchSize = 1024 * 16;

    /**
     * batch 没满的情况下默认等待 100 ms
     */
    private final int lingerMs = 100;

    /**
     * producer 的缓存为 64M
     */
    private final int bufferMemory = 1024 * 1024 * 64;

    /**
     * 需要确保写入副本 leader
     */
    private final String acks = "1";

    /**
     * 为了减少带宽，使用 lz4 压缩
     */
    private final String compressionType = "lz4";

    /**
     * 当 memory buffer 满了之后，send() 在抛出异常之前阻塞的最长时间
     */
    private final int blockMs = 10000;

    private final String serializerClass = "org.apache.kafka.common.serialization.StringSerializer";

    private final Properties properties;

    private KafkaProducer<String, String> producer;

    public MetricKafkaClientCollector(String address) {
        properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        properties.put(ProducerConfig.ACKS_CONFIG, acks);
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, blockMs);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializerClass);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass);
    }

    @Override
    public void start() {
        this.producer = new KafkaProducer<>(properties);
    }

    /**
     *
     * @param topic
     * @param message
     * @param callback
     * @param <T>
     */
    public <T extends TimeSeries> void send(String topic, T message, Callback callback) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(message);
        producer.send(new ProducerRecord<>(topic, FastJsonConvertUtil.convertObjectToJSON(message)),
                callback);
    }

    @Override
    public void shutdown() {
        this.producer.close();
    }
}
