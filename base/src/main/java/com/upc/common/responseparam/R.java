package com.upc.common.responseparam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class R<T> {
    private Integer code;

    private String message;

    private T data;

    private Long materialId;

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public R(Integer code, String message, T data, Long materialId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.materialId = materialId;
    }

    public static <T> R<T> commonReturn(Integer code, String message, T data) {
        return new R<>(code, message, data);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "请求成功", data);
    }

    public static <T> R<T> ok() {
        return R.ok(null);
    }

    public static <T> R<T> fail() {
        return R.fail("请求错误");
    }

    public static <T> R<T> fail(String message) {
        return new R<>(400, message, null);
    }

    public static <T> R<T> unauthorized() {
        return new R<>(401, "请登陆后重试", null);
    }

    public static <T> R<PageBaseReturnParam<T>> page(PageBaseReturnParam<T> param) {
        return R.ok(param);
    }

}