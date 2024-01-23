package cn.ray.gateway.client.core;

import cn.ray.gateway.client.GatewayInvoker;
import cn.ray.gateway.client.GatewayProtocol;
import cn.ray.gateway.client.GatewayService;
import cn.ray.gateway.client.support.dubbo.DubboConstants;
import cn.ray.gateway.common.config.DubboServiceInvoker;
import cn.ray.gateway.common.config.HttpServiceInvoker;
import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.common.config.ServiceInvoker;
import cn.ray.gateway.common.constants.BasicConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ray
 * @date 2023/12/5 09:14
 * @description 客户端注解扫描类, 用于扫描所有的用户定义的 @GatewayService 和 @GatewayInvoker, 解析构建 ServiceDefinition 资源服务定义
 */
public class GatewayAnnotationScanner {

    private GatewayAnnotationScanner() {}

    private static class SingletonHolder {
        private static final GatewayAnnotationScanner INSTANCE = new GatewayAnnotationScanner();
    }

    public static GatewayAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 扫描传入的Bean对象，最终返回一个ServiceDefinition
     * @param bean
     * @param args
     * @return
     */
    public synchronized ServiceDefinition scanBuilder(Object bean, Object... args) {
        Class<?> clazz = bean.getClass();
        boolean isPresent = clazz.isAnnotationPresent(GatewayService.class);
        
        if (isPresent) {
            GatewayService gatewayService = clazz.getAnnotation(GatewayService.class);
            String serviceId = gatewayService.serviceId();
            GatewayProtocol protocol = gatewayService.protocol();
            String patternPath = gatewayService.patternPath();
            String version = gatewayService.version();

            ServiceDefinition serviceDefinition = new ServiceDefinition();
            Map<String /* invokerPath */, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();

            Method[] methods = clazz.getMethods();
            if (methods != null && methods.length > 0) {
                for (Method method : methods) {
                    GatewayInvoker gatewayInvoker = method.getAnnotation(GatewayInvoker.class);
                    if (gatewayInvoker == null) {
                        continue;
                    }

                    String path = gatewayInvoker.path();

                    // TODO GatewayInvoker 注解加入 ruleId 供研发人员配置

                    switch (protocol) {
                        case HTTP:
                            HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path, bean, method);
                            invokerMap.put(path, httpServiceInvoker);
                            break;
                        case DUBBO:
                            ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                            DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                            String dubboVersion = dubboServiceInvoker.getVersion();

                            // 将服务定义版本修正为 dubbo 版本
                            if (!StringUtils.isBlank(dubboVersion)) {
                                version = dubboVersion;
                            }

                            invokerMap.put(path, dubboServiceInvoker);
                            break;
                        default:
                            break;
                    }
                }
            }

            // 服务定义唯一ID: serviceId:version
            serviceDefinition.setUniqueId(serviceId + BasicConstants.COLON_SEPARATOR + version);
            // 服务唯一ID
            serviceDefinition.setServiceId(serviceId);
            // 服务版本
            serviceDefinition.setVersion(version);
            // 服务调用协议
            serviceDefinition.setProtocol(protocol.getCode());
            // ANT 路径匹配
            serviceDefinition.setPatternPath(patternPath);
            // TODO: 服务默认开启, 后续可支持在注解中配置 enable = true / false
            serviceDefinition.setEnable(true);
            // 服务调用列表信息
            serviceDefinition.setInvokerMap(invokerMap);
            return serviceDefinition;
        }
        
        return null;
    }

    /**
     * 构建HttpServiceInvoker对象
     * @param path
     * @param bean
     * @param method
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path, Object bean, Method method) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();

        // 调用路径
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 构建DubboServiceInvoker对象
     * @param path
     * @param serviceBean
     * @param method
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();

        // 调用路径
        dubboServiceInvoker.setInvokerPath(path);

        // 方法名
        String methodName = method.getName();
        // 注册中心地址
        String registryAddress = serviceBean.getRegistry().getAddress();
        // 接口全类名
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setRegistryAddress(registryAddress);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        // 参数类型名
        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            parameterTypes[i] = classes[i].getName();
        }

        dubboServiceInvoker.setParameterTypes(parameterTypes);

        // 服务超时时间
        Integer serviceTimeout = serviceBean.getTimeout();
        if (serviceTimeout == null || serviceTimeout == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if (providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if (providerTimeout == null || providerTimeout == 0) {
                    serviceTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    serviceTimeout = providerTimeout;
                }
            }
        }

        dubboServiceInvoker.setTimeout(serviceTimeout);

        // dubbo 版本
        dubboServiceInvoker.setVersion(serviceBean.getVersion());

        return dubboServiceInvoker;
    }
}
