package com.tieba.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class API {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private CloseableHttpClient client = HttpClients.createDefault();
    public String serverUrl = "https://tieba.baidu.com";
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    public void remakeClient() {
        client = HttpClients.createDefault();
    }
    
    @SneakyThrows
    public Response GET(String url, String... params) {
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
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
        
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                try {
                    return objectMapper.readValue(responseBody, Response.class);
                } catch (JsonProcessingException e) {
                    // 不需要再次调用 EntityUtils.toString(response.getEntity())
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
