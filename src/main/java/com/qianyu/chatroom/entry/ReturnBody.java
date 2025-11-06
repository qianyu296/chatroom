package com.qianyu.chatroom.entry;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReturnBody {
    private String code;
    private String message;
    private Object data;
    public ReturnBody(String code,String message,Object data){
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public ReturnBody(String code,String message){
        this.code = code;
        this.message = message;
    }
    public static ReturnBody Success(String code,String message,Object data){
        return new ReturnBody(code,message,data);
    }
    public static ReturnBody Error(String code,String message){
        return new ReturnBody(code,message);
    }
    public static ReturnBody Success(String code,String message){
        return new ReturnBody(code,message);
    }
}
