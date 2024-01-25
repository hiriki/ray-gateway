package cn.ray.gateway.core.helper;

import cn.ray.gateway.common.config.DubboServiceInvoker;
import cn.ray.gateway.common.enums.ResponseCode;
import cn.ray.gateway.common.exception.DubboConnectException;
import cn.ray.gateway.core.GatewayConfig;
import cn.ray.gateway.core.GatewayConfigLoader;
import cn.ray.gateway.core.balance.DubboLoadBalance;
import cn.ray.gateway.core.context.AttributeKey;
import cn.ray.gateway.core.context.DubboRequest;
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

    private final ApplicationConfig applicationConfig;

    private volatile ReferenceConfigCache referenceConfigCache = ReferenceConfigCache.getCache();

    private final Cache<String, GenericService> cache = DefaultCacheManager.getInstance().create(DefaultCacheManager.GENERIC_SERVICE_CONFIG_CACHE_ID);

    private DubboReferenceHelper() {
        this.applicationConfig = new ApplicationConfig(APPLICATION_CONFIG_NAME);
        this.applicationConfig.setOwner(APPLICATION_OWNER);
        this.applicationConfig.setOrganization(APPLICATION_ORGANIZATION);
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

    public static DubboRequest buildDubboRequest(DubboServiceInvoker dubboServiceInvoker, Object[] parameters) {
        DubboRequest dubboRequest = new DubboRequest();
        dubboRequest.setRegistriesStr(dubboServiceInvoker.getRegistryAddress());
        dubboRequest.setInterfaceClass(dubboServiceInvoker.getInterfaceClass());
        dubboRequest.setMethodName(dubboServiceInvoker.getMethodName());
        dubboRequest.setParameterTypes(dubboServiceInvoker.getParameterTypes());
        dubboRequest.setArgs(parameters);
        dubboRequest.setTimeout(dubboServiceInvoker.getTimeout());
        dubboRequest.setVersion(dubboServiceInvoker.getVersion());
        return dubboRequest;
    }

    public CompletableFuture<Object> $invokeAsync(GatewayContext gatewayContext, DubboRequest dubboRequest) {
        //	内部封装，dubbo调用都要填充请求上下文供后续负载均衡使用
//        fillRpcContext(gatewayContext);
        //	创建泛化调用对象, 并进行缓存
        GenericService genericService = newGenericServiceForReg(dubboRequest.getRegistriesStr(),
                dubboRequest.getInterfaceClass(),
                dubboRequest.getTimeout(),
                dubboRequest.getVersion());

        try {
            //	执行泛化调用请求
            CompletableFuture<Object> completableFuture = genericService.$invokeAsync(dubboRequest.getMethodName(),
                    dubboRequest.getParameterTypes(),
                    dubboRequest.getArgs());

            return completableFuture;
        } catch (Exception e) {
            throw new DubboConnectException(e, gatewayContext.getUniqueId(),
                    gatewayContext.getOriginRequest().getPath(),
                    dubboRequest.getInterfaceClass(),
                    dubboRequest.getMethodName(),
                    ResponseCode.DUBBO_REQUEST_ERROR);
        }
    }

    private GenericService newGenericServiceForReg(String registriesStr,
                                                   String interfaceClass,
                                                   int timeout,
                                                   String version) {

        String key = registriesStr + ":" + interfaceClass + ":" + version;

        // 缓存 GenericService, 避免重复调用创建成本较高
        GenericService genericService = cache.get(key, s -> {
            //	默认RegistryConfig
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registriesStr);
            registryConfig.setCheck(false);
            registryConfig.setTimeout(20000);
            if (registriesStr.indexOf("://") < 0) {
                registryConfig.setProtocol("zookeeper");
            }
            return newGenericService(Arrays.asList(registryConfig), interfaceClass, timeout, version);
        });
        return genericService;
    }

    private GenericService newGenericService(List<RegistryConfig> registries, String interfaceClass, int timeout, String version) {
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
//        GatewayConfig gatewayConfig = GatewayConfigLoader.gatewayConfig();
//        int dubboConnections = gatewayConfig.getDubboConnections();

        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<GenericService>();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistries(registries);
        referenceConfig.setTimeout(timeout);
        referenceConfig.setGeneric("true");
        referenceConfig.setInterface(interfaceClass);
        referenceConfig.setAsync(true);
        referenceConfig.setCheck(false);
        referenceConfig.setLoadbalance(DubboLoadBalance.NAME);

        referenceConfig.setParameters(new HashMap<>());
        referenceConfig.getParameters().put(DISPATCHER_KEY, "direct");
        referenceConfig.getParameters().put(SHARE_CONNECTIONS_KEY, String.valueOf(4));
        if (StringUtils.isNotEmpty(version)) {
            referenceConfig.setVersion(version);
        }
        return referenceConfigCache.getCache().get(referenceConfig);
    }
}
