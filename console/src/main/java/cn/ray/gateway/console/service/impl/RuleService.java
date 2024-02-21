package cn.ray.gateway.console.service.impl;

import cn.ray.gateway.common.config.Rule;
import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.FastJsonConvertUtil;
import cn.ray.gateway.common.utils.Pair;
import cn.ray.gateway.console.service.IRuleService;
import cn.ray.gateway.discovery.api.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ray
 * @date 2024/2/12 22:19
 * @description
 */
@Service
public class RuleService implements IRuleService {

    @Autowired
    private RegistryService registryService;

    @Override
    public List<Rule> getRuleList(String namespace, String env) throws Exception {
        String prefixPath = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.RULE_PREFIX;
        List<Pair<String, String>> list = registryService.getListByPrefixPath(prefixPath);
        List<Rule> rules = new ArrayList<>();
        for(Pair<String, String> pair : list) {
            String p = pair.getObject1();
            if (p.equals(prefixPath)) {
                continue;
            }
            String json = pair.getObject2();
            Rule rule = FastJsonConvertUtil.convertJSONToObject(json, Rule.class);
            rules.add(rule);
        }
        return rules;
    }

    @Override
    public void addRule(String namespace, String env, Rule rule) throws Exception {
        String prefixPath = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.RULE_PREFIX;
        String key = prefixPath + RegistryService.PATH + rule.getId();
        String value = FastJsonConvertUtil.convertObjectToJSON(rule);
        registryService.registerPermanentNode(key, value);
    }

    @Override
    public void updateRule(String namespace, String env, Rule rule) throws Exception {
        String prefixPath = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.RULE_PREFIX;
        String key = prefixPath + RegistryService.PATH + rule.getId();
        String value = FastJsonConvertUtil.convertObjectToJSON(rule);
        registryService.registerPermanentNode(key, value);
    }

    @Override
    public void deleteRule(String namespace, String env, String ruleId) {
        String prefixPath = RegistryService.PATH
                + namespace + BasicConstants.BAR_SEPARATOR + env
                + RegistryService.RULE_PREFIX;
        String key = prefixPath + RegistryService.PATH + ruleId;
        registryService.deleteByKey(key);
    }
}
