package cn.ray.gateway.core.context;

import cn.ray.gateway.common.constants.BasicConstants;
import cn.ray.gateway.common.utils.TimeUtil;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.cookie.Cookie;


import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Ray
 * @date 2023/11/7 19:59
 * @description 核心请求自定义实现
 */
@Slf4j
public class GatewayRequest implements GatewayRequestMutable {

    /**
     * FullHttpRequest: 在header里面必须要有该属性：uniqueId
     * 表示服务的唯一性ID: serviceId:version , 用于路由服务
     */
    @Getter
    private final String uniqueId;

    /**
     * 请求进入网关的开始时间戳
     */
    @Getter
    private final long beginTime;

    /**
     * 请求支持的字符集
     */
    @Getter
    private final Charset charset;

    /**
     * 客户端IP: 用于流控、黑白名单
     */
    @Getter
    private final String clientIp;

    /**
     * 请求的地址: ip:port
     */
    @Getter
    private final String host;

    /**
     * 请求路径: /xxx/xx/xxx
     */
    @Getter
    private final String path;

    /**
     * uri: /xxx/xx/xxx?attr1=value1&attr2=value2
     */
    @Getter
    private final String uri;

    /**
     * 请求方法: get/post/put...
     */
    @Getter
    private final HttpMethod httpMethod;

    /**
     * 请求格式
     */
    @Getter
    private final String contentType;

    /**
     * 请求头信息
     */
    @Getter
    private final HttpHeaders headers;

    /**
     * 参数解析器
     */
    @Getter
    private final QueryStringDecoder queryStringDecoder;

    /**
     * FullHttpRequest
     */
    @Getter
    private final FullHttpRequest fullHttpRequest;

    /**
     * 请求体
     */
    private String body;

    /**
     * 请求对象中的 cookie
     */
    private Map<String, io.netty.handler.codec.http.cookie.Cookie> cookieMap;

    /**
     * 	请求的时候定义的post参数集合
     */
    private Map<String, List<String>> postParameters;

    /***************** GatewayRequestMutable:可修改的请求变量 	**********************/

    /**
     * 可修改的 scheme , 默认为 http://
     */
    private String modifyScheme;

    /**
     * 可修改的host
     */
    private String modifyHost;

    /**
     * 可修改的path
     */
    private String modifyPath;

    /**
     * 构建下游请求时的Http请求构建器
     */
    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId, Charset charset, String clientIp,
                          String host, String uri, HttpMethod httpMethod,
                          String contentType, HttpHeaders headers, FullHttpRequest fullHttpRequest) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.contentType = contentType;
        this.headers = headers;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.path = this.queryStringDecoder.path();
        this.fullHttpRequest = fullHttpRequest;

        this.modifyHost = this.host;
        this.modifyPath = this.path;
        this.modifyScheme = BasicConstants.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder = new RequestBuilder();
        this.requestBuilder.setMethod(getHttpMethod().name());
        this.requestBuilder.setHeaders(headers);
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());
        ByteBuf content = fullHttpRequest.content();
        if (Objects.nonNull(content)) {
            this.requestBuilder.setBody(content.nioBuffer());
        }
    }

    /**
     * 获取请求体
     * @return
     */
    public String getBody() {
        if(StringUtils.isEmpty(this.body)) {
            this.body = fullHttpRequest.content().toString(charset);
        }
        return this.body;
    }

    /**
     * 获取指定的cookie
     * @param key
     * @return Cookie
     */
    public io.netty.handler.codec.http.cookie.Cookie getCookie(String key) {
        if (cookieMap == null) {
            cookieMap = new HashMap<>();
            String cookieStr = this.getHeaders().get(HttpHeaderNames.COOKIE);
            Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for(io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                cookieMap.put(cookie.name(), cookie);
            }
        }
        return cookieMap.get(key);
    }

    /**
     * 获取指定名称的指定参数值
     * @param name
     * @return
     */
    public List<String> getQueryParametersMultiple(String name){
        return this.queryStringDecoder.parameters().get(name);
    }

    public List<String> getPostParametersMultiple(String name) {
        String body = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder postParamDecoder = new QueryStringDecoder(body, false);
                postParameters = postParamDecoder.parameters();
            }

            if (postParameters == null || postParameters.isEmpty()) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            try {
                return Lists.newArrayList(JsonPath.read(body, name).toString());
            } catch (Exception e) {
                //	ignore
                log.error("#GatewayRequest# getPostParametersMultiple JsonPath解析失败，jsonPath: {}, body: {}", name, body, e);
            }
        }
        return null;
    }

    @Override
    public void setModifyHost(String host) {
        this.modifyHost = host;
    }

    @Override
    public String getModifyHost() {
        return this.modifyHost;
    }

    @Override
    public void setModifyPath(String path) {
        this.modifyPath = path;
    }

    @Override
    public String getModifyPath() {
        return this.modifyPath;
    }

    @Override
    public void addHeader(CharSequence key, String value) {
        requestBuilder.addHeader(key, value);
    }

    @Override
    public void setHeader(CharSequence key, String value) {
        requestBuilder.setHeader(key, value);
    }

    @Override
    public void addQueryParam(String key, String value) {
        requestBuilder.addQueryParam(key, value);
    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void addFormParam(String key, String value) {
        if (isFormPost()) {
            requestBuilder.addFormParam(key, value);
        }
    }

    @Override
    public void setRequestTimeout(int requestTimeout) {
        requestBuilder.setRequestTimeout(requestTimeout);
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }

    @Override
    public String  getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }

    public boolean isFormPost() {
        return HttpMethod.POST.equals(httpMethod) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    public boolean isJsonPost() {
        return HttpMethod.POST.equals(httpMethod) &&
                contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }
}
