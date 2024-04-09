package cn.ray.gateway.core.context;

import cn.ray.gateway.common.config.Rule;
import cn.ray.gateway.common.utils.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Ray
 * @date 2023/11/10 08:35
 * @description 网关请求上下文核心对象
 */
public class GatewayContext extends BasicContext {

    private final GatewayRequest gatewayRequest;

    private GatewayResponse gatewayResponse;

    private final Rule rule;

    private boolean isGray;

    private GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean isKeepAlive,
                          GatewayRequest gatewayRequest, Rule rule) {
        super(protocol, nettyCtx, isKeepAlive);
        this.gatewayRequest = gatewayRequest;
        this.rule = rule;
        this.isGray = false;
    }

    public static class Builder {

        private String protocol;

        private ChannelHandlerContext nettyCtx;

        private GatewayRequest gatewayRequest;

        private Rule rule;

        private boolean isKeepAlive;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setGatewayRequest(GatewayRequest gatewayRequest) {
            this.gatewayRequest = gatewayRequest;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean isKeepAlive) {
            this.isKeepAlive = isKeepAlive;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(this.protocol, "protocol不能为空");
            AssertUtil.notNull(this.nettyCtx, "nettyCtx不能为空");
            AssertUtil.notNull(gatewayRequest, "gatewayRequest不能为空");
            AssertUtil.notNull(rule, "rule不能为空");
            return new GatewayContext(protocol, nettyCtx, isKeepAlive, gatewayRequest, rule);
        }
    }

    @Override
    public Rule getRule() {
        return this.rule;
    }

    @Override
    public GatewayRequest getRequest() {
        return this.gatewayRequest;
    }

    /**
     * 调用该方法就是获取原始请求内容，不去做任何修改动作
     * @return
     */
    public GatewayRequest getOriginRequest() {
        return this.gatewayRequest;
    }

    /**
     * 调用该方法区分于原始的请求对象操作，主要就是标记将要进行修改
     * @return
     */
    public GatewayRequest getRequestMutable() {
        return this.gatewayRequest;
    }

    @Override
    public void releaseRequest() {
        if(isRequestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(gatewayRequest.getFullHttpRequest());
        }
    }

    @Override
    public void setResponse(Object response) {
        this.gatewayResponse = (GatewayResponse) response;
    }

    @Override
    public GatewayResponse getResponse() {
        return this.gatewayResponse;
    }

    /**
     * 获取必要的上下文参数，如果没有则抛出IllegalArgumentException
     * @param key 必需
     * @return
     * @param <T>
     */
    public <T> T getRequiredAttribute(AttributeKey<T> key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * 获取指定key的上下文参数，如果没有则返回第二个参数的默认值
     * @param key
     * @param defaultValue
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * 根据过滤器id获取对应的过滤器配置信息
     * @param filterId
     * @return
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取上下文中请求唯一的UniqueId
     * @return serviceId:version
     */
    public String getUniqueId() {
        return gatewayRequest.getUniqueId();
    }

    public boolean isGray() {
        return isGray;
    }

    public void setGray(boolean gray) {
        isGray = gray;
    }
}
