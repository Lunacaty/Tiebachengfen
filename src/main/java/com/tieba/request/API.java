package com.tieba.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class API {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public String serverUrl = "https://tieba.baidu.com";
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build())
            .build();
    
    
    public void remakeClient() {
        client = HttpClients.createDefault();
    }
    
    @SneakyThrows
    public Response GET(String url, String... params) {
        
        URIBuilder uriBuilder = new URIBuilder(serverUrl + url);
        if (params.length > 0) {
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    uriBuilder.addParameter(keyValue[0], keyValue[1]);
                } else {
                    uriBuilder.addParameter(param, "");
                }
            }
        }
        
        HttpGet request = new HttpGet(uriBuilder.build());
        request.setHeader("Accept", "application/json; charset=UTF-8");
        
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                try {
                    return objectMapper.readValue(responseBody, Response.class);
                } catch (JsonProcessingException e) {
                    return new Response(200, "请求成功", responseBody);
                }
            } else {
                return Response.error("请求失败", response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.error("请求失败");
        }
    }
    
    public API(String part) {
        this.serverUrl += part;
    }
    
    public API() {
    }
    
    private String processParams(String... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for (String param : params) {
            sb.append("&").append(param);
        }
        return sb.toString();
    }
}
