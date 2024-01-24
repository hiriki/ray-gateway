package cn.ray.gateway.common.config;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Ray
 * @date 2023/11/20 08:21
 * @description 服务实例：一个服务定义会对应多个服务实例
 */
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = -7559569289189228478L;

    /**
     * 服务实例唯一ID: ip:port
     */
    protected String serviceInstanceId;

    /**
     * 服务定义唯一ID:
     */
    protected String uniqueId;

    /**
     * 服务实例地址: ip:port
     */
    protected String address;

    /**
     * 标签信息
     */
    protected String tags;

    /**
     * 权重
     */
    protected Integer weight;

    /**
     * 服务注册时间戳: 用于负载均衡、预热
     */
    protected long registerTime;

    /**
     * 服务实例是否启用
     */
    protected boolean enable = true;

    /**
     * 服务实例对应的版本号
     */
    protected String version;

    public ServiceInstance() {
    }

    public ServiceInstance(String serviceInstanceId, String uniqueId, String address, String tags, Integer weight, long registerTime, boolean enable, String version) {
        this.serviceInstanceId = serviceInstanceId;
        this.uniqueId = uniqueId;
        this.address = address;
        this.tags = tags;
        this.weight = weight;
        this.registerTime = registerTime;
        this.enable = enable;
        this.version = version;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(this == null || getClass() != o.getClass()) return false;
        ServiceInstance serviceInstance = (ServiceInstance) o;
        return Objects.equals(this.serviceInstanceId, serviceInstance.serviceInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstanceId);
    }
}
