package cn.ray.gateway.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ray
 * @date 2024/2/8 21:43
 * @description 多个插件合并实现，并且安全执行插件逻辑
 */
@Slf4j
public class MultiplePluginImpl implements Plugin {

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private Map<String, Plugin> plugins = new HashMap<String, Plugin>();

    public MultiplePluginImpl(Map<String, Plugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            //	独立的线程去初始化,不影响网关启动
            new Thread(() -> plugins.values().forEach(plugin -> {
                try {
                    plugin.init();
                } catch (Throwable t) {
                    log.error("MultiplePluginImpl, 插件初始化失败：{}", plugin.getClass().getName(), t);
                    initialized.set(false);
                }
            })).start();
        }
    }

    @Override
    public void destroy() {
        plugins.values().forEach(plugin -> {
            try {
                plugin.destroy();
            } catch (Throwable t) {
                log.error("MultiplePluginImpl, 插件关闭失败：{}", plugin.getClass().getName(), t);
            }
        });
    }

    @Override
    public Plugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }

}
