package cn.ray.gateway.core.netty.processor.filter.post;

import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.common.utils.Pair;
import cn.ray.gateway.core.GatewayConfigLoader;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;
import cn.ray.gateway.core.rolling.RollingNumber;
import cn.ray.gateway.core.rolling.RollingNumberEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Ray
 * @date 2024/2/6 19:35
 * @description 后置过滤器-统计分析
 */
@Filter(
        id = ProcessorFilterConstants.STATISTICS_POST_FILTER_ID,
        name = ProcessorFilterConstants.STATISTICS_POST_FILTER_NAME,
        type = ProcessorFilterType.POST,
        order = ProcessorFilterConstants.STATISTICS_POST_FILTER_ORDER
)
public class StatisticsPostFilter extends AbstractEntryProcessorFilter<StatisticsPostFilter.Config> {

    public static final Integer windowSize = 60 * 1000;

    public static final Integer bucketSize = 60;

    private RollingNumber rollingNumber;

    private Thread conusmerThread;

    public StatisticsPostFilter() {
        super(StatisticsPostFilter.Config.class);
        MetricConsumer metricConsumer = new MetricConsumer();
        this.rollingNumber = new RollingNumber(windowSize, bucketSize,
                "ray-gateway",
                metricConsumer.getMetricQueue());
        conusmerThread = new Thread(metricConsumer);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {
            StatisticsPostFilter.Config config = (StatisticsPostFilter.Config) args[0];
            if(config.isUseRollingNumber()) {
                conusmerThread.start();
                rollingNumber(context, args);
            }
        } finally {
            context.terminated();
            super.fireNext(context, args);
        }
    }

    private void rollingNumber(Context context, Object[] args) {
        Throwable throwable = context.getThrowable();
        if(throwable == null) {
            rollingNumber.increment(RollingNumberEvent.SUCCESS);
        }
        else {
            rollingNumber.increment(RollingNumberEvent.FAILURE);
        }

        //	请求开始的时间
        long SRTime = context.getSRTime();
        //	路由的开始时间(route ---> service)
        long RSTime = context.getRSTime();
        //	路由的接收请求时间（service --> route）
        long RRTime = context.getRRTime();
        //	请求结束（写回请求的时间）
        long SSTime = context.getSSTime();

        //	整个生命周期的耗时
        long requestTimeout = SSTime - SRTime;
        long defaultRequestTimeout = GatewayConfigLoader.gatewayConfig().getRequestTimeout();

        if(requestTimeout > defaultRequestTimeout) {
            rollingNumber.increment(RollingNumberEvent.REQUEST_TIMEOUT);
        }

        // 路由转发耗时
        long routeTimeout = RRTime - RSTime;
        long defaultRouteTimeout = GatewayConfigLoader.gatewayConfig().getRouteTimeout();
        if(routeTimeout > defaultRouteTimeout) {
            rollingNumber.increment(RollingNumberEvent.ROUTE_TIMEOUT);
        }
    }

    public class MetricConsumer implements Runnable {

        private ArrayBlockingQueue<Pair<String, Long>> metricQueue = new ArrayBlockingQueue<>(65535);

        private volatile boolean isRunning = false;

        public void start() {
            isRunning = true;
        }

        public void shutdown() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    Pair<String, Long> pair = metricQueue.take();
                    String key = pair.getObject1();
                    Long value = pair.getObject2();

                    // report 上报

                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        public ArrayBlockingQueue<Pair<String, Long>> getMetricQueue() {
            return this.metricQueue;
        }

        public void setMetricQueue(ArrayBlockingQueue<Pair<String, Long>> metricQueue) {
            this.metricQueue = metricQueue;
        }

        public boolean isRunning() {
            return this.isRunning;
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private boolean useRollingNumber = true;
    }
}
