package com.muxcraft.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Result<Void>> handleBizException(BizException e) {
        Result<Void> body = Result.fail(e.getResultCode(), e.getMessage());
        HttpStatus status = HttpStatus.resolve(e.getResultCode().getCode());
        if (status == null) {
            status = HttpStatus.OK;
        }
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorMessageConstants.VALIDATION_FAILED;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ResultCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorMessageConstants.VALIDATION_FAILED;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ResultCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.fail(ResultCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.fail(ResultCode.PAYLOAD_TOO_LARGE, ErrorMessageConstants.UPLOAD_FILE_TOO_LARGE);
    }

    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipartException(MultipartException e) {
        log.warn("文件上传解析异常: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, ErrorMessageConstants.UPLOAD_FILE_INVALID);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据唯一约束冲突: {}", e.getMessage());
        return Result.fail(ResultCode.CONFLICT, ErrorMessageConstants.DATA_DUPLICATE);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性异常: {}", e.getMessage());
        String message = ErrorMessageConstants.DATA_CONFLICT;
        if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
            message = ErrorMessageConstants.DATA_DUPLICATE;
        }
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("文件访问被拒绝: {}", e.getMessage());
        return Result.fail(ResultCode.INTERNAL_ERROR, ErrorMessageConstants.FILE_ACCESS_DENIED);
    }

    @ExceptionHandler(SecurityException.class)
    public Result<Void> handleSecurityException(SecurityException e) {
        log.error("安全权限异常: {}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN, ErrorMessageConstants.FORBIDDEN_ACCESS);
    }

    @ExceptionHandler(IllegalStateException.class)
    public Result<Void> handleIllegalStateException(IllegalStateException e) {
        log.error("非法状态异常: {}", e.getMessage(), e);
        String message = e.getMessage() != null ? e.getMessage() : ErrorMessageConstants.INTERNAL_STATE_ERROR;
        return Result.fail(ResultCode.INTERNAL_ERROR, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        String message = e.getMessage() != null ? e.getMessage() : ErrorMessageConstants.PARAM_ERROR;
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.fail(ResultCode.INTERNAL_ERROR, ErrorMessageConstants.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的请求类型: {}", e.getMessage());
        return Result.fail(ResultCode.UNSUPPORTED_MEDIA_TYPE, ErrorMessageConstants.MEDIA_TYPE_NOT_SUPPORTED);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理异常, type={}", e.getClass().getName(), e);
        return Result.fail(ResultCode.INTERNAL_ERROR, ErrorMessageConstants.SERVER_ERROR);
    }
}