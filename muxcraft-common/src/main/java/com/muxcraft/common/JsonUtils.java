package com.muxcraft.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    public static <T> T parse(String json, Class<T> clazz) {
        T obj;
        try {
            obj = OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new BizException(ResultCode.BAD_REQUEST, "data 字段 JSON 格式错误: " + e.getOriginalMessage());
        }
        Set<ConstraintViolation<T>> violations = VALIDATOR_FACTORY.getValidator().validate(obj);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("；"));
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
        return obj;
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}