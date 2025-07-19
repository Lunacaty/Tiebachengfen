package com.tieba.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.client.config.CookieSpecs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class API {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public String serverUrl = "https://tieba.baidu.com";
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .setConnectTimeout(Timeout.ofMilliseconds(5000))
                    .setResponseTimeout(Timeout.ofMilliseconds(5000))
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))
                    .build())
            .build();
    
    private static final Semaphore semaphore = new Semaphore(50);
    public void remakeClient() {
        client = HttpClients.createDefault();
    }
    
    @SneakyThrows
    public Response GET(String url, String... params) {
        
        
        URIBuilder uriBuilder = new URIBuilder(serverUrl + url);
//        System.out.println("GET " + serverUrl + url);
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
        System.out.println(uriBuilder.build().toString());
        
        HttpGet request = new HttpGet(uriBuilder.build());
        request.setHeader("Accept", "application/json; charset=UTF-8");
        
        semaphore.acquire();
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                try {
                    semaphore.release();
                    return objectMapper.readValue(responseBody, Response.class);
                } catch (JsonProcessingException e) {
                    semaphore.release();
                    return new Response(200, "请求成功", responseBody);
                }
            } else {
                semaphore.release();
                return Response.error("请求失败", response.getCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            semaphore.release();
            return Response.error("请求失败");
        }
    }
    
    @SneakyThrows
    public Response POST(String url, Map<String, String> body) {
        
        URIBuilder uriBuilder = new URIBuilder(serverUrl + url);
        
//        System.out.println("GET " + serverUrl + url);
        
        HttpPost request = new HttpPost(uriBuilder.build());
        request.setHeader("Content-Type","application/x-www-form-urlencoded");
        
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : body.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        
        semaphore.acquire();
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                try {
                    semaphore.release();
                    return objectMapper.readValue(responseBody, Response.class);
                } catch (JsonProcessingException e) {
                    semaphore.release();
                    return new Response(200, "请求成功", responseBody);
                }
            } else {
                semaphore.release();
                return Response.error("请求失败", response.getCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            semaphore.release();
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
