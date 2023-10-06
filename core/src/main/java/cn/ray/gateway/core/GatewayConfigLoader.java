package cn.ray.gateway.core;

import cn.ray.gateway.common.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Ray
 * @date 2023/10/6 07:29
 * @description 网关配置信息加载类
 * 	网关配置加载规则：优先级顺序如下：高的优先级会覆盖掉低的优先级
 * 		运行参数(最高) ->  JVM 参数  -> 环境变量  -> 配置文件  -> 内部 GatewayConfig 对象的默认属性值(最低);
 */
@Slf4j
public class GatewayConfigLoader {

    private final static String CONFIG_FILE = "gateway.properties";

    private final static String CONFIG_ENV_PREFIX = "GATEWAY_";

    private final static String CONFIG_JVM_PREFIX = "gateway.";

    private final static GatewayConfigLoader INSTANCE = new GatewayConfigLoader();

    private GatewayConfigLoader() {
    }

    private GatewayConfig gatewayConfig = new GatewayConfig();

    public static GatewayConfigLoader getInstance() {
        return INSTANCE;
    }

    public static GatewayConfig gatewayConfig() {
        return INSTANCE.gatewayConfig;
    }

    public GatewayConfig load(String[] args) {

        //	加载逻辑：从上到下依次覆盖

        //	1. 配置文件
        {
            InputStream is = GatewayConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if(is != null) {
                Properties properties = new Properties();
                try {
                    properties.load(is);
                    PropertiesUtil.properties2Object(properties, gatewayConfig);
                } catch (IOException e) {
                    //	warn
                    log.warn("#RapidConfigLoader# load config file: {} is error", CONFIG_FILE, e);
                } finally {
                    if(is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            //	ignore
                        }
                    }
                }
            }
        }

        //	2. 环境变量
        {
            Map<String, String> env = System.getenv();
            Properties properties = new Properties();
            properties.putAll(env);
            PropertiesUtil.properties2Object(properties, gatewayConfig, CONFIG_ENV_PREFIX);
        }

        //	3. jvm参数
        {
            Properties properties = System.getProperties();
            PropertiesUtil.properties2Object(properties, gatewayConfig, CONFIG_JVM_PREFIX);
        }

        //	4. 运行参数: --xxx=xxx --enable=true  --port=1234
        {
            if(args != null && args.length > 0) {
                Properties properties = new Properties();
                for(String arg : args) {
                    if(arg.startsWith("--") && arg.contains("=")) {
                        properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                    }
                }
                PropertiesUtil.properties2Object(properties, gatewayConfig);
            }
        }

        return gatewayConfig;
    }
}
