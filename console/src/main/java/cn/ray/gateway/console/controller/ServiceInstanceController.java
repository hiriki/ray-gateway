package cn.ray.gateway.console.controller;

import cn.ray.gateway.common.config.ServiceInstance;
import cn.ray.gateway.console.dto.ServiceInstanceDTO;
import cn.ray.gateway.console.service.IServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/21 10:02
 * @description
 */
@RestController
@RequestMapping("/serviceInstance")
public class ServiceInstanceController {

    @Autowired
    private IServiceInstanceService serviceInstanceService;

    @GetMapping("/getList")
    public List<ServiceInstance> getList(@RequestParam("namespace") String namespace,
                                         @RequestParam("env") String env,
                                         @RequestParam("uniqueId")String uniqueId) throws Exception{
        List<ServiceInstance> list = serviceInstanceService.getServiceInstanceList(namespace, env, uniqueId);
        return list;
    }

    @PutMapping("/updateEnable")
    public void updateEnable(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
        if(serviceInstanceDTO != null) {
            serviceInstanceService.updateEnable(
                    serviceInstanceDTO.getNamespace(),
                    serviceInstanceDTO.getEnv(),
                    serviceInstanceDTO.getUniqueId(),
                    serviceInstanceDTO.getServiceInstanceId(),
                    serviceInstanceDTO.isEnable());
        }
    }

    @PutMapping("/updateTags")
    public void updateTags(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
        if(serviceInstanceDTO != null) {
            serviceInstanceService.updateTags(
                    serviceInstanceDTO.getNamespace(),
                    serviceInstanceDTO.getEnv(),
                    serviceInstanceDTO.getUniqueId(),
                    serviceInstanceDTO.getServiceInstanceId(),
                    serviceInstanceDTO.getTags());
        }
    }

    @PutMapping("/updateWeight")
    public void updateWeight(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
        if(serviceInstanceDTO != null) {
            serviceInstanceService.updateWeight(
                    serviceInstanceDTO.getNamespace(),
                    serviceInstanceDTO.getEnv(),
                    serviceInstanceDTO.getUniqueId(),
                    serviceInstanceDTO.getServiceInstanceId(),
                    serviceInstanceDTO.getWeight());
        }
    }
}
