package cn.ray.gateway.console;

import cn.ray.gateway.discovery.api.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.ServiceLoader;

/**
 * @author Ray
 * @date 2024/2/10 22:48
 * @description
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(GatewayConsoleProperties.class)
@ConditionalOnProperty(prefix = GatewayConsoleProperties.GATEWAY_CONSOLE_PREFIX, name = {"registryAddress", "namespace", "env"})
public class MainConfig {

    @Resource
    private GatewayConsoleProperties gatewayConsoleProperties;

    @Bean
    public RegistryService registryService() {
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
        for(RegistryService registryService : serviceLoader) {
            registryService.initialized(gatewayConsoleProperties.getRegistryAddress());
            return registryService;
        }
        return null;
    }

    @Bean
    public ConsumerContainer consumerContainer(GatewayConsoleProperties gatewayConsoleProperties) {
        String kafkaAddress = gatewayConsoleProperties.getKafkaAddress();
        String groupId = gatewayConsoleProperties.getGroupId();
        String topicNamePrefix = gatewayConsoleProperties.getTopicNamePrefix();
        if(StringUtils.isBlank(kafkaAddress)) {
            log.warn("#MainConfig.consumerContainer# kafkaAddress is null!");
            return null;
        }
        if(StringUtils.isBlank(groupId)) {
            log.warn("#MainConfig.consumerContainer# groupId is null!");
            return null;
        }
        if(StringUtils.isBlank(topicNamePrefix)) {
            log.warn("#MainConfig.consumerContainer# topicNamePrefix is null!");
            return null;
        }
        return new ConsumerContainer(gatewayConsoleProperties);
    }
}
