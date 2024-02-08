package cn.ray.gateway.core.plugin.metric.kafka;

import cn.ray.gateway.common.metric.TimeSeries;
import cn.ray.gateway.core.GatewayConfigLoader;
import cn.ray.gateway.core.plugin.Plugin;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ray
 * @date 2024/2/8 22:09
 * @description
 */
@Slf4j
public class MetricKafkaClientPlugin implements Plugin {

    private MetricKafkaClientCollector metricKafkaClientCollector;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private String address;

    @Override
    public boolean check() {
        this.address = GatewayConfigLoader.gatewayConfig().getKafkaAddress();
        if (!StringUtils.isBlank(this.address)) {
            return true;
        }
        return false;
    }

    @Override
    public void init() {
        if(check()) {
            // 初始化kafka
            this.metricKafkaClientCollector = new MetricKafkaClientCollector(this.address);
            this.metricKafkaClientCollector.start();
            this.initialized.compareAndSet(false, true);
        }
    }

    private boolean checkInit() {
        return this.initialized.get() && this.metricKafkaClientCollector != null;
    }

    @Override
    public void destroy() {
        if(checkInit()) {
            this.metricKafkaClientCollector.shutdown();
            this.initialized.compareAndSet(true, false);
        }
    }

    public <T extends TimeSeries> void send(T metric) {
        try {
            if(checkInit()) {
                metricKafkaClientCollector.send(metric.getDestination(), metric,
                        (metadata, exception) -> {
                            if (exception != null) {
                                log.error("#MetricKafkaClientSender# callback exception, metric: {}, {}", metric.toString(), exception.getMessage());
                            }
                        }
                );
            }
        } catch (Exception e) {
            log.error("#MetricKafkaClientSender# send exception, metric: {}", metric.toString(), e);
        }
    }

    public <T extends TimeSeries> void sendBatch(List<T> metricList) {
        for (T metric : metricList) {
            send(metric);
        }
    }

    @Override
    public Plugin getPlugin(String pluginName) {
        if(checkInit() && (MetricKafkaClientPlugin.class.getName()).equals(pluginName)) {
            return this;
        }
        throw new RuntimeException("#MetricKafkaClientPlugin# pluginName: " + pluginName + " is no matched");
    }
}
