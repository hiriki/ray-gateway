package cn.ray.gateway.core.context;

import cn.ray.gateway.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * @author Ray
 * @date 2023/11/7 08:04
 * @description 网关上下文接口定义
 */
public interface Context {

    /**
     * 正常运行状态, 表示当前请求正在执行过程中
     */
    int RUNNING = -1;

    /**
     * 写回标记状态, 标记当前Context/请求需要写回(并不一定是执行所有的过滤器链)
     */
    int WRITTEN = 0;

    /**
     * 写回成功状态, ctx.writeAndFlush(response), 防止并发下多次写回
     * 写回成功并不代表过滤器链结束, 存在后置处理器(流量监控、qps统计等)
     */
    int COMPLETED = 1;

    /**
     * 最终结束状态, 表示整个网关请求完毕
     */
    int TERMINATED = 2;



    /*************** -- 设置网关的状态系 -- ***************/

    /**
     * 设置上下文状态为正常运行状态
     */
    void running();

    /**
     * 设置上下文状态为写回标记状态
     */
    void written();

    /**
     * 设置上下文状态为写回成功状态
     */
    void completed();

    /**
     * 设置上下文状态为最终结束状态
     */
    void terminated();



    /*************** -- 判断网关的状态系 -- ***************/

    /**
     * 判断上下文状态是否为正常运行状态
     * @return T/F
     */
    boolean isRunning();

    /**
     * 判断上下文状态是否为写回标记状态
     * @return T/F
     */
    boolean isWritten();

    /**
     * 判断上下文状态是否为写回成功状态
     * @return T/F
     */
    boolean isCompleted();

    /**
     * 判断上下文状态是否为最终结束状态
     * @return T/F
     */
    boolean isTerminated();


    /**
     * 获取请求转换协议
     * @return
     */
    String getProtocol();

    /**
     * 获取规则
     * @return Rule
     */
    Rule getRule();

    /**
     * 获取请求对象
     * @return
     */
    Object getRequest();

    /**
     * 获取响应对象
     * @return
     */
    Object getResponse();

    /**
     * 设置响应对象
     * @param response
     */
    void setResponse(Object response);

    /**
     * 设置异常
     * @param throwable
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取异常
     * @return Throwable
     */
    Throwable getThrowable();

    /**
     * 获取上下文参数
     * @param key 对应的key
     * @return
     * @param <T>
     */
    <T> T getAttribute(AttributeKey<T> key);

    /**
     * 保存上下文属性信息
     * @param key 关键key
     * @param value 上下文参数值
     * @return
     * @param <T>
     */
    <T> T putAttribute(AttributeKey<T> key, T value);

    /**
     * 获取Netty的上下文对象
     * @return ChannelHandlerContext
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 是否长连接
     * @return
     */
    boolean isKeepAlive();

    /**
     * 释放请求资源
     */
    void releaseRequest();

    /**
     * 写回结束回调函数设置
     * @param consumer
     */
    void completedCallback(Consumer<Context> consumer);

    /**
     * 执行回调函数
     */
    void invokeCompletedCallback();
}
