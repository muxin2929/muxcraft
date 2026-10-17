package com.muxcraft.common;

/**
 * 全局异常/错误消息常量，统一维护便于修改
 * 面向前端用户展示，避免使用技术术语
 */
public final class ErrorMessageConstants {

    private ErrorMessageConstants() {
    }

    public static final String VALIDATION_FAILED = "输入内容不符合要求，请检查后重试";

    public static final String PARAM_ERROR = "提交的信息有误，请检查后重试";

    public static final String UPLOAD_FILE_TOO_LARGE = "图片太大，不能超过 2MB";

    public static final String UPLOAD_FILE_INVALID = "上传失败，图片格式不符合要求";

    public static final String DATA_DUPLICATE = "该内容已存在，无需重复添加";

    public static final String DATA_CONFLICT = "用户ID已存在，请重新输入";

    public static final String FILE_ACCESS_DENIED = "图片保存失败，请联系管理员";

    public static final String FORBIDDEN_ACCESS = "您没有权限执行此操作";

    public static final String INTERNAL_STATE_ERROR = "系统繁忙，请稍后重试";

    public static final String INTERNAL_SERVER_ERROR = "系统出了点问题，请稍后重试";

    public static final String MEDIA_TYPE_NOT_SUPPORTED = "不支持当前操作";

    public static final String SERVER_ERROR = "系统繁忙，请稍后再试";
}