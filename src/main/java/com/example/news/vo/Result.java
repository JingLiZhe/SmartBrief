package com.example.news.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    /**
     * 成功返回（带数据），默认消息 "success"
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功返回（带数据和自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 成功返回（无数据），默认消息 "success"
     * 适用于只表示操作成功、不需要返回数据的场景（如 DELETE、PUT 等）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 错误返回（自定义错误码和消息）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 错误返回（默认错误码 500）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}