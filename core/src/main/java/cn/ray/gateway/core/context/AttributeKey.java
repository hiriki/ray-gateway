package cn.ray.gateway.core.context;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.config.ServiceInvoker;
import cn.ray.gateway.common.enums.LoadBalanceStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ray
 * @date 2023/11/7 10:40
 * @description 属性上下文的抽象类，在其内部进行简单实现
 */
public abstract class AttributeKey<T> {

    private static final Map<String, AttributeKey<?>> NAMED_MAP = new HashMap<>();

    public static final AttributeKey<ServiceInvoker> HTTP_INVOKER = create(ServiceInvoker.class);

    public static final AttributeKey<ServiceInvoker> DUBBO_INVOKER = create(ServiceInvoker.class);

    /**
     * 存储所有服务实例的列表信息，负载均衡使用
     */
    public static final AttributeKey<Set<ServiceInstance>> MATCH_INSTANCES = create(Set.class);

    /**
     * 负载均衡后的实例信息
     */
    public static final AttributeKey<ServiceInstance> LOAD_INSTANCE = create(ServiceInstance.class);

    /**
     * dubbo负载均衡的策略使用
     */
    public static final AttributeKey<LoadBalanceStrategy> DUBBO_LOAD_BALANCE_STRATEGY = create(LoadBalanceStrategy.class);

    static {
        NAMED_MAP.put("HTTP_INVOKER", HTTP_INVOKER);
        NAMED_MAP.put("DUBBO_INVOKER", DUBBO_INVOKER);
        NAMED_MAP.put("MATCH_INSTANCES", MATCH_INSTANCES);
        NAMED_MAP.put("LOAD_INSTANCE", LOAD_INSTANCE);
        NAMED_MAP.put("DUBBO_LOAD_BALANCE_STRATEGY", DUBBO_LOAD_BALANCE_STRATEGY);
    }

    /**
     * 指定对象转换对应class类型
     * @param value 真实的数据对象值
     * @return
     */
    public abstract T cast(Object value);

    /**
     * 对外暴露创建 AttributeKey
     * @param valueClass 对应的泛型类
     * @return AttributeKey -> SimpleAttributeKey
     * @param <T>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> AttributeKey<T> create(final Class<? super T> valueClass) {
        return new SimpleAttributeKey(valueClass);
    }

    /**
     * 简单的属性 key 转换类
     * @param <T>
     */
    public static class SimpleAttributeKey<T> extends AttributeKey<T> {

        private final Class<T> valueClass;

        public SimpleAttributeKey(Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public T cast(Object value) {
            return valueClass.cast(value);
        }

        @Override
        public String toString() {
            if(valueClass != null) {
                return getClass().getName()
                        + "<" + valueClass.getName() + ">";
            }
            return super.toString();
        }
    }
}
