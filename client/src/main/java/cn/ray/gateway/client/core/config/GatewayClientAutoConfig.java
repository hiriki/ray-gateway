package cn.ray.gateway.client.core.config;

import cn.ray.gateway.client.support.dubbo.Dubbo27ClientRegistryManager;
import cn.ray.gateway.client.support.http.SpringMVCClientRegistryManager;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.Servlet;

/**
 * @author Ray
 * @date 2023/12/7 02:11
 * @description SpringBoot自动装配加载类
 */
@Configuration
@EnableConfigurationProperties(GatewayClientProperties.class)
@ConditionalOnProperty(prefix = GatewayClientProperties.PROPERTIES_PREFIX, name = {"registryAddress", "namespace"})
public class GatewayClientAutoConfig {

    @Autowired
    private GatewayClientProperties gatewayClientProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegistryManager.class)
    // 如果在之前已经存在就不需要再加载了
    // 可以在Spring Bean的生命周期上就进行注入
    public SpringMVCClientRegistryManager springMVCClientRegistryManager() {
        return new SpringMVCClientRegistryManager(gatewayClientProperties);
    }

    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(Dubbo27ClientRegistryManager.class)
    public Dubbo27ClientRegistryManager dubbo27ClientRegistryManager() {
        return new Dubbo27ClientRegistryManager(gatewayClientProperties);
    }
}
