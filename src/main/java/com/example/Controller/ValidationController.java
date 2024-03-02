package com.example.Controller;

import com.example.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class ValidationController {
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> handleValidationException( ValidationException e) {
        log.error("ValidationException occurred"+e.getMessage());
        // 处理验证异常的逻辑
        return RestBean.failure(400,"请求参数有误");
    }
}
