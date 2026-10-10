package com.muxcraft.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.nio.file.AccessDeniedException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        return Result.fail(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.fail(ResultCode.PAYLOAD_TOO_LARGE, "文件过大，单个文件不能超过2MB");
    }

    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipartException(MultipartException e) {
        log.warn("文件上传解析异常: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "文件上传失败，请检查文件格式和大小");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据唯一约束冲突: {}", e.getMessage());
        return Result.fail(ResultCode.CONFLICT, "数据已存在，请勿重复添加");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性异常: {}", e.getMessage());
        String message = "数据冲突，可能存在重复值或关联数据";
        if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
            message = "数据已存在，请勿重复添加";
        }
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("文件访问被拒绝: {}", e.getMessage());
        return Result.fail(ResultCode.INTERNAL_ERROR, "文件存储目录无写入权限");
    }

    @ExceptionHandler(SecurityException.class)
    public Result<Void> handleSecurityException(SecurityException e) {
        log.error("安全权限异常: {}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN, "权限不足，操作被拒绝");
    }

    @ExceptionHandler(IllegalStateException.class)
    public Result<Void> handleIllegalStateException(IllegalStateException e) {
        log.error("非法状态异常: {}", e.getMessage(), e);
        String message = e.getMessage() != null ? e.getMessage() : "服务器内部状态异常";
        return Result.fail(ResultCode.INTERNAL_ERROR, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        String message = e.getMessage() != null ? e.getMessage() : "参数错误";
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR, "服务器内部错误，请稍后重试");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的请求类型: {}", e.getMessage());
        return Result.fail(ResultCode.UNSUPPORTED_MEDIA_TYPE, "请求格式不支持");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理异常, type={}", e.getClass().getName(), e);
        return Result.fail(ResultCode.INTERNAL_ERROR, "服务端错误，请稍后重试");
    }
}