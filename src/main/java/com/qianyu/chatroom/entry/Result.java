package com.qianyu.chatroom.entry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Result {
    private String code;
    private String message;
    private Object data;
    public Result(String code, String message, Object data){
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public Result(String code, String message){
        this.code = code;
        this.message = message;
    }
    public static Result Success(String code, String message, Object data){
        return new Result(code,message,data);
    }
    public static Result Error(String code, String message){
        return new Result(code,message);
    }
    public static Result Success(String code, String message){
        return new Result(code,message);
    }
}
