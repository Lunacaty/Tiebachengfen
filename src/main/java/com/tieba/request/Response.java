package com.tieba.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private Integer code;
    private String msg;
    private Object data;
    
    public static Response error(String msg,int code){
        return new Response(code,msg,null);
    }
    
    public static Response error(String msg){
        return new Response(500,msg,null);
    }
}

