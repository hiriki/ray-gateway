package cn.ray.test.dubbo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Ray
 * @date 2023/12/15 22:48
 * @description
 */
@SpringBootApplication
@EnableDubbo
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
