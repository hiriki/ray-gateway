package cn.ray.gateway.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Ray
 * @date 2024/2/8 21:47
 * @description 插件管理器
 */
@Slf4j
public class PluginManager {

    private final MultiplePluginImpl multiplePlugin;

    private static final PluginManager INSTANCE = new PluginManager();

    public static Plugin getMultiplePlugins() {
        return INSTANCE.multiplePlugin;
    }

    private PluginManager() {
        //	SPI方式扫描所有插件实现
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        Map<String, Plugin> multiplePlugins = new HashMap<String, Plugin>();
        for (Plugin plugin : plugins) {
            if (!plugin.check()) {
                continue;
            }
            String pluginName = plugin.getClass().getName();
            multiplePlugins.put(pluginName, plugin);
            log.info("#PluginFactory# The Scanner Plugin is: {}", plugin.getClass().getName());
        }
        //	安全执行插件逻辑
        this.multiplePlugin = new MultiplePluginImpl(multiplePlugins);
        Runtime.getRuntime().addShutdownHook(new Thread(multiplePlugin::destroy, "Shutdown-Plugin"));
    }
}
