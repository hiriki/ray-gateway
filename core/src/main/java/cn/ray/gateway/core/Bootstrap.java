package cn.ray.gateway.core;

import cn.ray.gateway.core.discovery.RegistryManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ray
 * @date 2023/10/6 07:01
 * @description 网关启动入口
 */
@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        //	1. 加载网关的配置信息
        GatewayConfig gatewayConfig = GatewayConfigLoader.getInstance().load(args);

        //	2. 插件初始化的工作

        //	3. 初始化服务注册管理中心（服务注册管理器）, 监听动态配置的新增、修改、删除
        try {
            RegistryManager.getInstance().initialized(gatewayConfig);
        } catch (Exception e) {
            log.error("RegistryManager is failed", e);
        }
        //	4. 启动容器
        GatewayContainer container = new GatewayContainer(gatewayConfig);
        container.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                container.shutdown();
            }
        });
    }
}
