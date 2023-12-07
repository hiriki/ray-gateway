package cn.ray.gateway.common.config;

/**
 * @author Ray
 * @date 2023/11/14 20:05
 * @description Dubbo 服务调用
 */
public class DubboServiceInvoker extends AbstractServiceInvoker {

    /**
     * 注册中心地址
     */
    private String registryAddress;

    /**
     * 接口全类名
     */
    private String interfaceClass;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型列表
     */
    private String[] parameterTypes;

    /**
     * Dubbo 版本
     */
    private String version;

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(String interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
