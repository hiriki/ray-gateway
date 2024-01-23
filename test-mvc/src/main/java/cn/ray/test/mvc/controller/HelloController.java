package cn.ray.test.mvc.controller;

import cn.ray.gateway.client.GatewayInvoker;
import cn.ray.gateway.client.GatewayProtocol;
import cn.ray.gateway.client.GatewayService;
import cn.ray.test.mvc.entity.TestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

/**
 * @author Ray
 * @date 2023/12/14 20:12
 * @description
 */
@RestController
@GatewayService(patternPath = "/test*", protocol = GatewayProtocol.HTTP, serviceId = "hello")
public class HelloController {

    private volatile int count;

    @Autowired
    private ApplicationContext applicationContext;

    @GatewayInvoker(path = "/testGet")
    @GetMapping("/testGet")
    public String testGet() {
        return "testGet";
    }

    @GatewayInvoker(path = "/testPost")
    @PostMapping("/testPost")
    public String testPost() {
        count++;
        if(count >= 1e5) {
            System.err.println("<------ ray: ------>");
            count = 0;
        }
        return "ray";
    }

    @GatewayInvoker(path = "/testParam")
    @RequestMapping("/testParam")
    public String testParam(@RequestParam String name) {
//        count++;
//        if(count >= 1e5) {
            System.err.println("<------ testParam收到请求, name:" + name + " ------>");
//            count = 0;
//        }
        return name;
    }

    @GatewayInvoker(path = "/testEntity")
    @RequestMapping("/testEntity")
    public String testEntity(@RequestBody TestEntity testEntity) {
        String result = "testEntity result :" + testEntity.getName() + testEntity.getAge();
        return result;
    }
    
}
