package cn.ray.gateway.core.context;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * @author Ray
 * @date 2023/11/7 18:38
 * @description 请求在过滤器中可设置或改变的参数操作接口
 */
public interface GatewayRequestMutable {

    /**
     * 设置请求 host
     * @param host
     */
    void setModifyHost(String host);

    /**
     * 获取修改的请求 host
     * @return
     */
    String getModifyHost();

    /**
     * 设置请求路径
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取修改的请求路径
     * @return
     */
    String getModifyPath();

    /**
     * 添加请求头信息
     * @param key
     * @param value
     */
    void addHeader(CharSequence key, String value);

    /**
     * 设置请求头信息
     * @param key
     * @param value
     */
    void setHeader(CharSequence key, String value);

    /**
     * 添加请求的查询参数
     * @param key
     * @param value
     */
    void addQueryParam(String key, String value);

    /**
     * 添加或替换cookie
     * @param cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 添加form表单参数
     * @param key
     * @param value
     */
    void addFormParam(String key, String value);

    /**
     * 设置请求超时时间
     * @param requestTimeout
     */
    void setRequestTimeout(int requestTimeout);

    /**
     * 构建转发请求的请求对象
     * @return AsyncHttpClient Request
     */
    Request build();

    /**
     * 获取最终的路由路径
     * @return
     */
    String getFinalUrl();
}
