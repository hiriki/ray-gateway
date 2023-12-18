package cn.ray.test.dubbo.service;

/**
 * @author Ray
 * @date 2023/12/15 22:49
 * @description
 */
public interface HelloService {

    User sayHelloUser(String name);

    User sayHelloUser(User user, String name);

    User sayHelloUser(User user);
}
