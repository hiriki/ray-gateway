package cn.ray.gateway.dubbo;

import cn.ray.gateway.common.config.DubboServiceInvoker;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.core.context.DubboRequest;
import cn.ray.gateway.core.helper.DubboReferenceHelper;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ray
 * @date 2024/1/25 10:17
 * @description
 */
public class DubboGenericTest {

    @Test
    public void testSayHelloA() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath("/sayHelloUser/a");
        dubboServiceInvoker.setRegistryAddress("zookeeper://#:2183?backup=#:2182,#:2181");
        dubboServiceInvoker.setInterfaceClass("cn.ray.test.dubbo.service.HelloService");
        dubboServiceInvoker.setMethodName("sayHelloUser");
        dubboServiceInvoker.setParameterTypes(new String[] {"java.lang.String"});
        dubboServiceInvoker.setTimeout(5000);
        dubboServiceInvoker.setVersion(null);
        String paramStr = "[1234]";
        List<Object> list = FastJsonConvertUtil.convertJSONToArray(paramStr, Object.class);

        DubboRequest dubboRequest = DubboReferenceHelper.buildDubboRequest(dubboServiceInvoker, list.toArray());
        CompletableFuture<Object> completableFuture = DubboReferenceHelper.getInstance().$invokeAsync(null, dubboRequest);

        completableFuture.whenCompleteAsync((retValue, throwable) -> {
            System.err.println(retValue);
            System.err.println(throwable);
            countDownLatch.countDown();
        });

        countDownLatch.await();
    }
}
