package cn.ray.gateway.common.concurrent.queue.mpmc;
/**
 * <B>主类名称：</B>SpinPolicy<BR>
 * <B>概要说明：</B><BR>
 */
public enum SpinPolicy {
    WAITING,
    BLOCKING,
    SPINNING;
}