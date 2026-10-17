package com.muxcraft.common;

/**
 * 业务状态码枚举
 */
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "无权限操作"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "用户ID已存在"),
    PAYLOAD_TOO_LARGE(413, "图片过大"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持请求格式"),
    UNPROCESSABLE(422, "业务校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务端错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}