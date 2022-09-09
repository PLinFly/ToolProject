package org.tool.cn.utils;

import org.tool.cn.other.ResponseConstant;

public class Response<T> {

    private Integer code;

    private String message;

    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Response<T> success(){
        return makeResponse(ResponseConstant.SUCCESS_CODE, ResponseConstant.SUCCESS_MSG, null);
    }

    public Response<T> success(T data){
        return makeResponse(ResponseConstant.SUCCESS_CODE, ResponseConstant.SUCCESS_MSG, data);
    }

    public Response<T> error(String message){
        return makeResponse(500,message,null);
    }

    public Response<T> makeResponse(int code, String message, T returnData) {
        this.setCode(code);
        this.setMessage(message);
        this.setData(returnData);
        return this;
    }
}
