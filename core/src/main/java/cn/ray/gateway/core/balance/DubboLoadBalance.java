package cn.ray.gateway.core.balance;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.enums.LoadBalanceStrategy;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.GatewayResponseException;
import cn.ray.gateway.core.context.AttributeKey;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.helper.DubboReferenceHelper;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Constants;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ray
 * @date 2024/1/24 14:44
 * @description 使用dubbo的SPI扩展点实现
 */
public class DubboLoadBalance implements LoadBalance {

    public static final String NAME = "balance";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        System.err.println("---------------- DubboLoadBalance into  --------------");

        GatewayContext gatewayContext = (GatewayContext) RpcContext.getContext().get(DubboReferenceHelper.DUBBO_TRANSFER_CONTEXT);
        LoadBalanceStrategy loadBalanceStrategy = gatewayContext.getAttribute(AttributeKey.DUBBO_LOAD_BALANCE_STRATEGY);
        cn.ray.gateway.core.balance.LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceStrategy);

        Set<ServiceInstance> instances = new HashSet<>();
        for (Invoker<?> invoker : invokers) {
            instances.add(new ServiceInstanceWrapper<>(invoker, invocation));
        }

        // 	把 dubbo invokers 的服务实例列表设置到上下文对象里, 便于后续负载均衡处理
        gatewayContext.putAttribute(AttributeKey.MATCH_INSTANCES, instances);

        ServiceInstance serviceInstance = loadBalance.select(gatewayContext);
        if (serviceInstance instanceof ServiceInstanceWrapper) {
            return ((ServiceInstanceWrapper) serviceInstance).getInvoker();
        } else {
            throw new GatewayResponseException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    /**
     * dubbo invoker 包装进服务实例
     * @param <T>
     */
    public static class ServiceInstanceWrapper<T> extends ServiceInstance {
        private static final long serialVersionUID = -6254823227724967507L;

        private final Invoker<T> invoker;

        public ServiceInstanceWrapper(Invoker<T> invoker, Invocation invocation) {
            this.invoker = invoker;
            this.setServiceInstanceId(invoker.getUrl().getAddress());
            this.setAddress(invoker.getUrl().getAddress());
            this.setUniqueId(invoker.getUrl().getServiceKey());
            this.setRegisterTime(invoker.getUrl().getParameter(CommonConstants.TIMESTAMP_KEY, 0L));
            this.setWeight(invoker.getUrl().getMethodParameter(invocation.getMethodName(),
                    Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT));
            this.setVersion(invoker.getUrl().getParameter(CommonConstants.VERSION_KEY));
            this.setEnable(true);
        }

        public Invoker<T> getInvoker() {
            return invoker;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(this == null || getClass() != o.getClass()) return false;
            ServiceInstanceWrapper<?> serviceInstanceWrapper = (ServiceInstanceWrapper<?>)o;
            return Objects.equals(this.address, serviceInstanceWrapper.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.address);
        }
    }
}
