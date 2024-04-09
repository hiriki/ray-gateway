package cn.ray.gateway.console.controller;

import cn.ray.gateway.console.ConsumerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ray
 * @date 2024/4/9 16:59
 * @description
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private ConsumerContainer consumerContainer;

    @GetMapping("/qps")
    public void qps() throws Exception {
        consumerContainer.start();
    }
}
