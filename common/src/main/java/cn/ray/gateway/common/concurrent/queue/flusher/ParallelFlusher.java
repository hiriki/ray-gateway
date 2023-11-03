package cn.ray.gateway.common.concurrent.queue.flusher;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Ray
 * @date 2023/10/31 19:22
 * @description
 */
public class ParallelFlusher<E> implements Flusher<E> {

    private RingBuffer<Holder> ringBuffer;

    private EventListener<E> eventListener;

    private WorkerPool<Holder> workerPool;

    private ExecutorService executorService;

    private EventTranslatorOneArg<Holder, E> eventTranslator;

    private ParallelFlusher(Builder<E> builder) {

        this.executorService = Executors.newFixedThreadPool(
                builder.threadSize,
                new ThreadFactoryBuilder().setNameFormat("ParallelFlusher-" + builder.threadNamePrefix + "-pool-%d").build()
        );

        this.eventListener = builder.eventListener;

        this.eventTranslator = new HolderEventTranslator();

        // 创建 RingBuffer
        RingBuffer<Holder> ringBuffer = RingBuffer.create(
                builder.producerType,
                new HolderEventFactory(),
                builder.bufferSize,
                builder.waitStrategy
                );

        // 通过 RingBuffer 创建屏障
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        // 创建多个消费者数组: HolderWorkHandler
        @SuppressWarnings("unchecked")
        WorkHandler<Holder>[] workHandlers = new WorkHandler[builder.threadSize];
        for (int i = 0; i < builder.threadSize; i++) {
            workHandlers[i] = new HolderWorkerHandler();
        }

        //	构建多消费者工作池
        WorkerPool<Holder> workerPool = new WorkerPool<Holder>(
                ringBuffer,
                sequenceBarrier,
                new HolderExceptionHandler(),
                workHandlers
        );

        //	设置多个消费者的sequence序号 用于单独统计消费进度, 并且设置到 RingBuffer 中
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        this.workerPool = workerPool;

    }

    @Override
    public void add(E event) {
        final RingBuffer<Holder> temp = this.ringBuffer;
        if (temp == null) {
            onException(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
            return;
        }
        try {
            ringBuffer.publishEvent(eventTranslator, event);
        } catch (NullPointerException e) {
            onException(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), event);
        }
    }

    @Override
    public void add(E... events) {
        final RingBuffer<Holder> temp = this.ringBuffer;
        if (temp == null) {
            onException(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
            return;
        }
        try {
            ringBuffer.publishEvents(eventTranslator, events);
        } catch (NullPointerException e) {
            onException(this.eventListener, new IllegalStateException("ParallelFlusher is closed"), events);
        }
    }

    @Override
    public boolean tryAdd(E event) {
        final RingBuffer<Holder> temp = this.ringBuffer;
        if (temp == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvent(eventTranslator, event);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean tryAdd(E... events) {
        final RingBuffer<Holder> temp = this.ringBuffer;
        if (temp == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvents(eventTranslator, events);
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean isShutDown() {
        return this.ringBuffer == null;
    }

    @Override
    public void start() {
        this.ringBuffer = workerPool.start(executorService);
    }

    @Override
    public void shutdown() {
        RingBuffer<Holder> temp = ringBuffer;
        this.ringBuffer = null;

        if(temp==null) {
            return;
        }
        if(workerPool != null) {
            workerPool.drainAndHalt();
        }
        if(executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 建造者模式, 目的就是为了设置真实对象的属性，在创建真实对象的时候透传过去
     * @param <E>
     */
    public static class Builder<E> {

        /**
         * 生产消费模式
         */
        private ProducerType producerType = ProducerType.MULTI;

        /**
         * 环形缓冲大小
         */
        private int bufferSize = 16 * 1024;

        /**
         * 消费线程
         */
        private int threadSize = 1;

        /**
         * 线程前缀
         */
        private String threadNamePrefix;

        /**
         * 等待策略
         */
        private WaitStrategy waitStrategy = new BlockingWaitStrategy();

        /**
         * 消费监听
         */
        private EventListener<E>  eventListener;

        public Builder<E> setProducerType(ProducerType producerType) {
            Preconditions.checkNotNull(producerType);
            this.producerType = producerType;
            return this;
        }

        public Builder<E> setBufferSize(int bufferSize) {
            Preconditions.checkArgument(Integer.bitCount(bufferSize)==1);
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<E> setThreadSize(int threadSize) {
            Preconditions.checkArgument(threadSize > 0);
            this.threadSize = threadSize;
            return this;
        }

        public Builder<E> setThreadNamePrefix(String threadNamePrefix) {
            Preconditions.checkNotNull(threadNamePrefix);
            this.threadNamePrefix = threadNamePrefix;
            return this;
        }

        public Builder<E> setWaitStrategy(WaitStrategy waitStrategy) {
            Preconditions.checkNotNull(waitStrategy);
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder<E> setEventListener(EventListener<E> eventListener) {
            Preconditions.checkNotNull(eventListener);
            this.eventListener = eventListener;
            return this;
        }

        public ParallelFlusher<E> build() {
            return new ParallelFlusher<>(this);
        }
    }

    public interface EventListener<E> {
        void onEvent(E event);

        void onException(Throwable ex, long sequence, E event);
    }

    private class Holder {
        private E event;

        public void setEvent(E event) {
            this.event = event;
        }

        public String toString() {
            return "Holder event=" + event;
        }
    }

    private class HolderEventFactory implements EventFactory<Holder> {
        @Override
        public Holder newInstance() {
            return new Holder();
        }
    }

    private class HolderWorkerHandler implements WorkHandler<Holder> {
        @Override
        public void onEvent(Holder holder) throws Exception {
            eventListener.onEvent(holder.event);
            holder.setEvent(null);
        }
    }

    private class HolderExceptionHandler implements ExceptionHandler<Holder> {
        @Override
        public void handleEventException(Throwable throwable, long l, Holder holder) {
            try {
                // 自定义异常处理
                eventListener.onException(throwable,l,holder.event);
            } catch (Exception e) {
                // ignore...
            } finally {
                holder.setEvent(null);
            }
        }

        @Override
        public void handleOnStartException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }

    }

    private static <E> void onException(EventListener<E> listener, Throwable e, E event) {
        listener.onException(e, -1, event);
    }

    private static <E> void onException(EventListener<E> listener, Throwable e, @SuppressWarnings("unchecked") E... events) {
        for(E event : events) {
            onException(listener, e, event);
        }
    }

    private class HolderEventTranslator implements EventTranslatorOneArg<Holder, E> {
        @Override
        public void translateTo(Holder holder, long l, E e) {
            holder.setEvent(e);
        }
    }

}
