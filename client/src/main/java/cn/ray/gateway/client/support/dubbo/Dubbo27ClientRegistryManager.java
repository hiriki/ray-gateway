package cn.ray.gateway.client.support.dubbo;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ray
 * @date 2023/12/7 02:18
 * @description dubbo 2.7.x 客户端注册管理类实现
 */
@Slf4j
public class Dubbo27ClientRegistryManager extends AbstractClientRegistryManager implements ApplicationListener<ApplicationEvent>, EnvironmentAware {

    private Environment environment;

    private static final Set<Object> uniqueBeanSet = new HashSet<>();

    public Dubbo27ClientRegistryManager(GatewayClientProperties gatewayClientProperties) throws Exception{
        super(gatewayClientProperties);
    }

    @PostConstruct
    private void init() {
        String port = environment.getProperty(DubboConstants.DUBBO_PROTOCOL_PORT);
        if (StringUtils.isEmpty(port)) {
            log.error("Dubbo服务未启动");
            return;
        }
        // 标记服务启动
        whetherStart = true;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(!whetherStart) {
            return;
        }

        // Dubbo 框架允许将服务导出为远程服务，以便其他应用程序可以通过 Dubbo 协议调用这些服务
        // 当 Dubbo 服务成功导出时，会发布 ServiceBeanExportedEvent 事件，以通知应用程序相关的服务已经成功导出。
        // 一般情况下，Dubbo 服务的导出是在 Spring 容器初始化时完成的。
        // 当一个 ServiceBean（Dubbo 的服务配置实体）成功导出时，会触发 ServiceBeanExportedEvent 事件。
        if(applicationEvent instanceof ServiceBeanExportedEvent) {
            ServiceBean<?> serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();
            try {
                registerServiceBean(serviceBean);
            } catch (Exception e) {
                log.error("Dubbo 注册服务 ServiceBean 失败，ServiceBean = {}", serviceBean, e);
            }
        } else if(applicationEvent instanceof ApplicationStartedEvent){
            //	START:::
            System.err.println("******************************************");
            System.err.println("**         Dubbo Client Started         **");
            System.err.println("******************************************");
        }
    }

    /**
     * 注册Dubbo服务：从 ServiceBeanExportedEvent 获取ServiceBean对象
     * @param serviceBean
     * @throws Exception
     */
    private void registerServiceBean(ServiceBean<?> serviceBean) throws Exception {

        Object bean = serviceBean.getRef();
        if(uniqueBeanSet.add(bean)) {
            ServiceDefinition serviceDefinition = GatewayAnnotationScanner.getInstance().scanBuilder(bean, serviceBean);
            if (serviceDefinition != null) {
                // 设置环境
                serviceDefinition.setEnvType(getEnv());

                // 注册服务定义
                registerServiceDefinition(serviceDefinition);

                // 配置服务实例信息
                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtil.getLocalIp();
                Integer port = serviceBean.getProtocol().getPort();

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
                // 服务实例标签
                serviceInstance.setTags(getTags());

                // 注册服务实例
                registerServiceInstance(serviceInstance);
            }
        }
    }

}
