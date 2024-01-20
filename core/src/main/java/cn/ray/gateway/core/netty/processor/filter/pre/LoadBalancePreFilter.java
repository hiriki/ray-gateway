package cn.ray.gateway.core.netty.processor.filter.pre;

import cn.ray.gateway.common.config.DynamicConfigManager;
import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.constants.GatewayProtocol;
import cn.ray.gateway.common.constants.ProcessorFilterConstants;
import cn.ray.gateway.common.enums.LoadBalanceStrategy;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.GatewayResponseException;
import cn.ray.gateway.core.balance.LoadBalance;
import cn.ray.gateway.core.balance.LoadBalanceFactory;
import cn.ray.gateway.core.context.AttributeKey;
import cn.ray.gateway.core.context.Context;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.context.GatewayRequest;
import cn.ray.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import cn.ray.gateway.core.netty.processor.filter.Filter;
import cn.ray.gateway.core.netty.processor.filter.FilterConfig;
import cn.ray.gateway.core.netty.processor.filter.ProcessorFilterType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * @author Ray
 * @date 2024/1/18 10:08
 * @description 前置过滤器-负载均衡
 */
@Filter(
        id = ProcessorFilterConstants.LOAD_BALANCE_PRE_FILTER_ID,
        name = ProcessorFilterConstants.LOAD_BALANCE_PRE_FILTER_NAME,
        type = ProcessorFilterType.PRE,
        order = ProcessorFilterConstants.LOAD_BALANCE_PRE_FILTER_ORDER
)
public class LoadBalancePreFilter extends AbstractEntryProcessorFilter<LoadBalancePreFilter.Config> {

    public LoadBalancePreFilter(Class<FilterConfig> filterConfigClass) {
        super(LoadBalancePreFilter.Config.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        try {
            GatewayContext gatewayContext = (GatewayContext)context;
            LoadBalancePreFilter.Config config = (LoadBalancePreFilter.Config)args[0];
            LoadBalanceStrategy loadBalanceStrategy = config.getBalanceStrategy();
            String protocol = gatewayContext.getProtocol();
            switch (protocol) {
                case GatewayProtocol.HTTP:
                    doHttpLoadBalance(gatewayContext, loadBalanceStrategy);
                    break;
                case GatewayProtocol.DUBBO:
                    doDubboLoadBalance(gatewayContext, loadBalanceStrategy);
                    break;
                default:
                    break;
            }
        } finally {
            super.fireNext(context, args);;
        }
    }

    private void doHttpLoadBalance(GatewayContext gatewayContext, LoadBalanceStrategy loadBalanceStrategy) {
        GatewayRequest gatewayRequest = gatewayContext.getRequest();
        String uniqueId = gatewayRequest.getUniqueId();

        // 获取对应指定服务下的所有实例, 并放入网关上下文中, 便于后续进行负载均衡
        Set<ServiceInstance> instances = DynamicConfigManager.getInstance().getServiceInstancesByUniqueId(uniqueId);
        gatewayContext.putAttribute(AttributeKey.MATCH_INSTANCES, instances);

        // 通过过滤器配置获取对应的负载均衡策略
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceStrategy);

        // 调用负载均衡实现，选择一个实例
        ServiceInstance serviceInstance = loadBalance.select(gatewayContext);

        if (serviceInstance == null) {
            //	如果服务实例没有找到：终止请求继续执行，显式抛出异常
            gatewayContext.terminated();
            throw new GatewayResponseException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }

        // 设置可修改的服务host，为当前选择的实例地址, 之后由网关通过 RouteFilter 转发请求到该服务实例
        gatewayContext.getRequestMutable().setModifyHost(serviceInstance.getAddress());
    }

    private void doDubboLoadBalance(GatewayContext gatewayContext, LoadBalanceStrategy loadBalanceStrategy) {
        //	将负载均衡策略设置到上下文中即可，由 dubbo LoadBalance去进行使用：SPI USED
        gatewayContext.putAttribute(AttributeKey.DUBBO_LOAD_BALANCE_STRATEGY, loadBalanceStrategy);
    }

    /**
     * 负载均衡前置过滤器相关配置
     */
    @Getter
    @Setter
    public static class Config extends FilterConfig {
        public LoadBalanceStrategy balanceStrategy = LoadBalanceStrategy.ROUND_ROBIN;
    }
}
