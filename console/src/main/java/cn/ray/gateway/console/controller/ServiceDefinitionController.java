package cn.ray.gateway.console.controller;

import cn.ray.gateway.common.config.ServiceDefinition;
import cn.ray.gateway.console.dto.ServiceDefinitionDTO;
import cn.ray.gateway.console.dto.ServiceInstanceDTO;
import cn.ray.gateway.console.service.IServiceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/21 08:23
 * @description
 */
@RestController
@RequestMapping("/serviceDefinition")
public class ServiceDefinitionController {

    @Autowired
    private IServiceDefinitionService serviceDefinitionService;

    @GetMapping("/getList")
    public List<ServiceDefinition> getList(@RequestParam("namespace") String namespace, @RequestParam("env") String env) throws Exception {
        List<ServiceDefinition> list = serviceDefinitionService.getServiceDefinitionList(namespace, env);
        return list;
    }

    @PutMapping("/updatePatternPathByUniqueId")
    public void updatePatternPathByUniqueId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
        if(serviceDefinitionDTO != null && serviceDefinitionDTO.getPatternPath()!= null) {
            serviceDefinitionService.updatePatternPathByUniqueId(
                    serviceDefinitionDTO.getNamespace(),
                    serviceDefinitionDTO.getEnv(),
                    serviceDefinitionDTO.getUniqueId(),
                    serviceDefinitionDTO.getPatternPath());
        }
    }

    @PutMapping("/updateEnableByUniqueId")
    public void updateEnableByUniqueId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
        if(serviceDefinitionDTO != null) {
            serviceDefinitionService.updateEnableByUniqueId(
                    serviceDefinitionDTO.getNamespace(),
                    serviceDefinitionDTO.getEnv(),
                    serviceDefinitionDTO.getUniqueId(),
                    serviceDefinitionDTO.isEnable());
        }
    }
}
