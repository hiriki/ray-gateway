package cn.ray.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Ray
 * @date 2023/11/7 15:49
 * @description 基础上下文抽象类, 提供基础的通用方法供继承方使用
 */
public abstract class BasicContext implements Context {

    protected final String protocol;

    protected final ChannelHandlerContext nettyCtx;

    protected final boolean isKeepAlive;

    /**
     * 上下文的 status 状态标识
     */
    protected volatile int status = Context.RUNNING;

    /**
     * 保存所有的上下文参数集合
     */
    protected final Map<AttributeKey<?>, Object> attributes = new HashMap<>();

    /**
     * 在请求过程中出现异常则设置异常对象
     */
    protected Throwable throwable;

    /**
     * 定义是否已经释放请求资源
     */
    protected final AtomicBoolean isRequestReleased = new AtomicBoolean(false);

    /**
     * 写回结束后的回调函数集合
     */
    protected List<Consumer<Context>> completedCallbacks;


    /**
     * SR(Server[Core] Received):       网关作为服务端接收请求
     * SS(Server[Core] Send):		    网关作为服务端写回请求
     * RS(Route Send):		    		网关作为客户端发送请求
     * RR(Route Received): 				网关作为客户端收到请求
     */
    protected long sRTime;

    protected long sSTime;

    protected long rSTime;

    protected long rRTime;

    public BasicContext(String protocol, ChannelHandlerContext nettyCtx, boolean isKeepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.isKeepAlive = isKeepAlive;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return this.nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return this.isKeepAlive;
    }

    @Override
    public void running() {
        this.status = Context.RUNNING;
    }

    @Override
    public void written() {
        this.status = Context.WRITTEN;
    }

    @Override
    public void completed() {
        this.status = Context.COMPLETED;
    }

    @Override
    public void terminated() {
        this.status = Context.TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return this.status == Context.RUNNING;
    }

    @Override
    public boolean isWritten() {
        return this.status == Context.WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return this.status == Context.COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return this.status == Context.TERMINATED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(AttributeKey<T> key) {
        return (T) attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T putAttribute(AttributeKey<T> key, T value) {
        return (T) attributes.put(key, value);
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public void releaseRequest() {
        // 释放请求资源同时将其标识为已经释放
        this.isRequestReleased.compareAndSet(false, true);
    }

    @Override
    public void completedCallback(Consumer<Context> consumer) {
        if (completedCallbacks == null) {
            completedCallbacks = new ArrayList<>();
        }
        completedCallbacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallback() {
        if(completedCallbacks != null) {
            completedCallbacks.forEach(call -> call.accept(this));
        }
    }

    @Override
    public long getSRTime() {
        return this.sRTime;
    }

    @Override
    public void setSRTime(long sRTime) {
        this.sRTime = sRTime;
    }

    @Override
    public long getSSTime() {
        return this.sSTime;
    }

    @Override
    public void setSSTime(long sSTime) {
        this.sSTime = sSTime;
    }

    @Override
    public long getRSTime() {
        return this.rSTime;
    }

    @Override
    public void setRSTime(long rSTime) {
        this.rSTime = rSTime;
    }

    @Override
    public long getRRTime() {
        return this.rRTime;
    }

    @Override
    public void setRRTime(long rRTime) {
        this.rRTime = rRTime;
    }
}
