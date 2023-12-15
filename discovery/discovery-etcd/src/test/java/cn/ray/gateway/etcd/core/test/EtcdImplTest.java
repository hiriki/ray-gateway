package cn.ray.gateway.etcd.core.test;

import cn.ray.gateway.etcd.api.EtcdClient;
import cn.ray.gateway.etcd.core.EtcdClientImpl;
import io.etcd.jetcd.KeyValue;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @author Ray
 * @date 2023/12/12 19:11
 * @description
 */
public class EtcdImplTest {

    @Test
    public void test() throws Exception {

        String registryAddress = "#";

        EtcdClient etcdClient = new EtcdClientImpl(registryAddress, true);

        System.err.println("etcdClient: " + etcdClient);

        KeyValue keyValue = etcdClient.getKey("name");

        System.err.println("key: " + keyValue.getKey().toString(Charset.defaultCharset()) + ", value: " + keyValue.getValue().toString(Charset.defaultCharset()));
    }
}
