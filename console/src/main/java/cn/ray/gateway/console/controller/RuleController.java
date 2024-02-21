package cn.ray.gateway.console.controller;

import cn.ray.gateway.common.config.Rule;
import cn.ray.gateway.console.dto.RuleDTO;
import cn.ray.gateway.console.service.IRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ray
 * @date 2024/2/12 22:33
 * @description
 */
@RestController
@RequestMapping("/rule")
public class RuleController {

    @Autowired
    private IRuleService ruleService;

    @GetMapping("/getList")
    public List<Rule> getList(@RequestParam("namespace") String namespace, @RequestParam("env") String env) throws Exception {
        List<Rule> list = ruleService.getRuleList(namespace, env);
        return list;
    }

    @PostMapping("/add")
    public void addRule(@RequestBody RuleDTO ruleDTO) throws Exception {
        if(ruleDTO != null) {
            Rule rule = new Rule();
            rule.setId(ruleDTO.getId());
            rule.setName(ruleDTO.getName());
            rule.setProtocol(rule.getProtocol());
            rule.setOrder(ruleDTO.getOrder());
            rule.setFilterConfigs(ruleDTO.getFilterConfigs());
            ruleService.addRule(ruleDTO.getNamespace(), ruleDTO.getEnv(), rule);
        }
    }

    @PutMapping("/update")
    public void updateRule(@RequestBody RuleDTO ruleDTO) throws Exception {
        if(ruleDTO != null) {
            Rule rule = new Rule();
            rule.setId(ruleDTO.getId());
            rule.setName(ruleDTO.getName());
            rule.setProtocol(rule.getProtocol());
            rule.setOrder(ruleDTO.getOrder());
            rule.setFilterConfigs(ruleDTO.getFilterConfigs());
            ruleService.updateRule(ruleDTO.getNamespace(), ruleDTO.getEnv(), rule);
        }
    }

    @DeleteMapping("/delete")
    public void deleteRule(@RequestBody RuleDTO ruleDTO) {
        if(ruleDTO != null) {
            ruleService.deleteRule(ruleDTO.getNamespace(), ruleDTO.getEnv(), ruleDTO.getId());
        }
    }
}
