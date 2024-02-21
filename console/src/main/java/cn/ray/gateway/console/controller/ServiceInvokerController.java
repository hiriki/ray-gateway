package cn.ray.gateway.console.controller;

import cn.ray.gateway.common.config.ServiceInvoker;
import cn.ray.gateway.console.service.impl.ServiceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/21 10:08
 * @description
 */
@RestController
@RequestMapping("/serviceInvoker")
public class ServiceInvokerController {

    @Autowired
    private ServiceDefinitionService serviceDefinitionService;

    @GetMapping("/getListByUniqueId")
    public List<ServiceInvoker> getListByUniqueId(@RequestParam("namespace") String namespace,
                                                  @RequestParam("env") String env,
                                                  @RequestParam("uniqueId")String uniqueId) throws Exception{
        List<ServiceInvoker> list = serviceDefinitionService.getServiceInvokerByUniqueId(namespace, env, uniqueId);
        return list;
    }

    @PostMapping("/bindingRuleId")
    public void bindingRuleId(@RequestParam("namespace") String namespace,
                              @RequestParam("env") String env,
                              @RequestParam("uniqueId")String uniqueId,
                              @RequestParam("invokerPath")String invokerPath,
                              @RequestParam("ruleId")String ruleId) throws Exception {

        serviceDefinitionService.serviceInvokerBindingRuleId(namespace, env, uniqueId, invokerPath, ruleId);
    }
}
