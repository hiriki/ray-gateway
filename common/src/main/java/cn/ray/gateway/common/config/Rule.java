package cn.ray.gateway.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ray
 * @date 2023/11/9 15:06
 * @description 规则模型
 */
@Data
public class Rule implements Comparable<Rule>, Serializable {

    private static final long serialVersionUID = 2540640682854847548L;

    /**
     * 规则ID, 全局唯一
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则对应的协议
     */
    private String protocol;

    /**
     * 规则排序，用于以后做一个路径绑定多种规则，但是只能最终执行一个规则（按照该属性做优先级判断）
     */
    private Integer order;

    /**
     * 规则过滤器集合
     */
    private Set<FilterConfig> filterConfigs = new HashSet<>();

    /**
     * 向规则里面添加指定的过滤器
     * @param filterConfig
     * @return
     */
    public boolean addFilterConfig(FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }

    /**
     * 根据指定的 filterId 获取 FilterConfig
     * @param filterId
     * @return FilterConfig
     */
    public FilterConfig getFilterConfig(String filterId){
        for(FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    /**
     * 根据指定的 filterId 判断其是否在当前 Rule 中存在
     * @param id
     * @return
     */
    public boolean hasFilterId(String id) {
        for(Rule.FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare = Integer.compare(this.getOrder(), o.getOrder());
        if(orderCompare == 0) {
            return this.getId().compareTo(o.getId());
        }
        return orderCompare;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if((o == null) || getClass() != o.getClass()) return false;
        Rule that = (Rule) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class FilterConfig {

        /**
         * 过滤器唯一ID
         */
        private String id;

        /**
         * 过滤器配置信息：json string
         */
        private String config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if((o == null) || getClass() != o.getClass()) return false;
            FilterConfig that = (FilterConfig) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}
