package cn.ray.gateway.common.config;

/**
 * @author Ray
 * @date 2023/11/14 19:59
 * @description 服务调用抽象类, 提供通用的基础实现以及一些默认的配置
 */
public abstract class AbstractServiceInvoker implements ServiceInvoker {

    protected String invokerPath;

    protected String ruleId;

    protected int timeout = 5000;

    @Override
    public String getInvokerPath() {
        return this.invokerPath;
    }

    @Override
    public void setInvokerPath(String invokerPath) {
        this.invokerPath = invokerPath;
    }

    @Override
    public String getRuleId() {
        return this.ruleId;
    }

    @Override
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public int getTimeout() {
        return this.timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
