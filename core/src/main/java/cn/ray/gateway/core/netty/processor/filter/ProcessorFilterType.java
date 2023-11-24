package cn.ray.gateway.core.netty.processor.filter;

/**
 * @author Ray
 * @date 2023/11/22 13:36
 * @description 过滤器的类型枚举
 */
public enum ProcessorFilterType {

    PRE("PRE", "前置过滤器"),

    ROUTE("ROUTE", "中置过滤器"),

    ERROR("ERROR", "异常过滤器"),

    POST("POST", "后置过滤器");

    private final String code;

    private final String message;

    ProcessorFilterType(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
