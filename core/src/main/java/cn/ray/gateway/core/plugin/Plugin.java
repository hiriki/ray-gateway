package cn.ray.gateway.core.plugin;

/**
 * @author Ray
 * @date 2024/2/8 21:41
 * @description 插件生命周期管理接口
 */
public interface Plugin {

    default boolean check() {
        return true;
    }

    void init();

    void destroy();

    Plugin getPlugin(String pluginName);
}
