package cn.ray.gateway.core.helper;

import cn.ray.gateway.common.config.DubboServiceInvoker;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.core.balance.DubboLoadBalance;
import cn.ray.gateway.core.context.AttributeKey;
import cn.ray.gateway.core.context.GatewayContext;
import cn.ray.gateway.core.netty.processor.cache.DefaultCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.apache.dubbo.remoting.Constants.DISPATCHER_KEY;
import static org.apache.dubbo.rpc.protocol.dubbo.Constants.SHARE_CONNECTIONS_KEY;

/**
 * @author Ray
 * @date 2024/1/24 14:54
 * @description 泛化调用辅助类
 */
@SuppressWarnings("all")
public class DubboReferenceHelper {

    public static final String DUBBO_TRANSFER_CONTEXT = "DUBBO_TRANSFER_CONTEXT";

    private static final String APPLICATION_CONFIG_NAME = "gateway-consumer";

    private static final String APPLICATION_OWNER = "gateway";

    private static final String APPLICATION_ORGANIZATION = "gateway";

    private static final int DEFAULT_TIMEOUT = 5000;

    private DubboReferenceHelper() {
    }

    private enum Singleton {

        INSTANCE;

        private DubboReferenceHelper singleton;

        Singleton() {
            singleton = new DubboReferenceHelper();
        }

        public DubboReferenceHelper getInstance() {
            return singleton;
        }

    }

    public static DubboReferenceHelper getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    private void fillRpcContext(GatewayContext gatewayContext) {
        //  dubbo 调用负载均衡所需参数
        RpcContext.getContext().set(DUBBO_TRANSFER_CONTEXT, gatewayContext);

        //  dubbo 附加信息传递
        if (gatewayContext.getAttribute(AttributeKey.DUBBO_ATTACHMENT) != null) {
            RpcContext.getContext().getAttachments().putAll(gatewayContext.getAttribute(AttributeKey.DUBBO_ATTACHMENT));
        }
    }
}
