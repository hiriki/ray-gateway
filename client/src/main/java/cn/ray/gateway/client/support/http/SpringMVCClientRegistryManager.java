package cn.ray.gateway.client.support.http;

import cn.ray.gateway.client.core.AbstractClientRegistryManager;
import cn.ray.gateway.client.core.GatewayAnnotationScanner;
import cn.ray.gateway.client.core.config.GatewayClientProperties;
import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.constants.GatewayConstants;
import cn.ray.gateway.common.utils.NetUtil;
import cn.ray.gateway.common.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ray
 * @date 2023/12/7 02:16
 * @description Http请求的客户端注册管理器
 */
@Slf4j
public class SpringMVCClientRegistryManager extends AbstractClientRegistryManager implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;

    private static final Set<Object> uniqueBeanSet = new HashSet<>();

    public SpringMVCClientRegistryManager(GatewayClientProperties gatewayClientProperties) throws Exception {
        super(gatewayClientProperties);
    }

    @PostConstruct
    private void init() {
        if (!ObjectUtils.allNotNull(serverProperties, serverProperties.getPort())) {
            return;
        }
        // 如果当前验证属性都为空 就进行初始化
        whetherStart = true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (!whetherStart) {
            return;
        }

        // WebServerInitializedEvent 表示 Web 服务器已初始化完成的事件。
        // 当 Spring 应用程序作为一个独立的 Web 服务器运行时，例如使用 Spring Boot 内嵌式容器（如Tomcat、Jetty或Undertow）启动时，会触发这个事件。
        // 通常，在应用程序启动并成功启动嵌入式 Web 服务器后，Spring Framework 会发布 WebServerInitializedEvent 事件，以通知应用程序已准备好接收 HTTP 请求。
        // 这是一个在应用程序中执行与 Web 服务器初始化相关逻辑的良好时机。
        // ServletWebServerInitializedEvent 是 WebServerInitializedEvent 的子类。
        // 与 WebServerInitializedEvent 类似，ServletWebServerInitializedEvent 也表示 Web 服务器已经初始化完成
        // 但它更具体地关注于使用 Servlet 容器的场景。
        if (applicationEvent instanceof WebServerInitializedEvent ||
                applicationEvent instanceof ServletWebServerInitializedEvent) {
            try {
                registerSpringMVC();
            } catch (Exception e) {
                log.error("#SpringMVCClientRegistryManager# registerSpringMVC error", e);
            }
        } else if(applicationEvent instanceof ApplicationStartedEvent){
            //	START:::
            System.err.println("******************************************");
            System.err.println("**        SpringMVC Client Started      **");
            System.err.println("******************************************");
        }

    }

    /**
     * 解析SpringMVC的事件，进行注册
     * @throws Exception
     */
    private void registerSpringMVC() throws Exception {
        // 在给定的 ApplicationContext 中查找所有类型为 RequestMappingHandlerMapping 的 Bean，包括当前上下文和其父上下文中的 Bean
        Map<String, RequestMappingHandlerMapping> handlerMappingMap = BeanFactoryUtils.
                beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class,
                        true, false);

        // 遍历所有的 RequestMappingHandlerMapping 实例
        for (RequestMappingHandlerMapping handlerMapping : handlerMappingMap.values()) {
            // 获取每个映射中的处理方法映射
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

            // 遍历处理方法映射
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                HandlerMethod handlerMethod = entry.getValue();

                // 获取处理方法所属的类（Controller 类）
                Class<?> clazz = handlerMethod.getBeanType();

                // 通过应用程序上下文获取该类对应的 Bean 实例
                Object bean = applicationContext.getBean(clazz);

                // 如果之前已经处理过该 Controller, 不再进行处理
                if (uniqueBeanSet.add(bean)) {
                    ServiceDefinition serviceDefinition = GatewayAnnotationScanner.getInstance().scanBuilder(bean);
                    if (serviceDefinition != null) {
                        // 设置环境
                        serviceDefinition.setEnvType(getEnv());

                        // 注册服务定义
                        registerServiceDefinition(serviceDefinition);

                        // 配置服务实例信息
                        ServiceInstance serviceInstance = new ServiceInstance();
                        String localIp = NetUtil.getLocalIp();
                        Integer port = serverProperties.getPort();

                        String serviceInstanceId = localIp + BasicConstants.COLON_SEPARATOR + port;
                        String uniqueId = serviceDefinition.getUniqueId();
                        String address = serviceInstanceId;
                        String version = serviceDefinition.getVersion();

                        // 服务实例唯一ID
                        serviceInstance.setServiceInstanceId(serviceInstanceId);
                        // 服务定义唯一ID
                        serviceInstance.setUniqueId(uniqueId);
                        // 服务实例地址
                        serviceInstance.setAddress(address);
                        // 服务实例权重
                        serviceInstance.setWeight(GatewayConstants.DEFAULT_WEIGHT);
                        // 服务实例注册时间戳
                        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                        // 服务实例对应版本
                        serviceInstance.setVersion(version);

                        // 注册服务实例
                        registerServiceInstance(serviceInstance);
                    }
                }
            }
        }
    }
}
