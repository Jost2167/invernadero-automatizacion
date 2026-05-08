package com.jost.invernadero.automatizacion.controller;

import com.jost.invernadero.automatizacion.dto.ApiError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String CODE_VALIDATION = "VALIDATION_ERROR";
    private static final String CODE_INTERNAL = "INTERNAL_ERROR";
    private static final String KEY_VALIDATION = "error.validation";
    private static final String KEY_INTERNAL = "error.internal";

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), resolveFieldMessage(fe, locale)))
                .toList();
        String message = messageSource.getMessage(KEY_VALIDATION, null, locale);
        return ResponseEntity.badRequest()
                .body(new ApiError(CODE_VALIDATION, message, fieldErrors));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String reason = ex.getReason();
        String message = reason != null
                ? messageSource.getMessage(reason, null, reason, locale)
                : ex.getStatusCode().toString();
        String code = reason != null ? reason : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ApiError(code, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(KEY_INTERNAL, null, locale);
        return ResponseEntity.internalServerError()
                .body(new ApiError(CODE_INTERNAL, message));
    }

    private String resolveFieldMessage(FieldError fe, Locale locale) {
        try {
            return messageSource.getMessage(fe, locale);
        } catch (Exception ignored) {
            return fe.getDefaultMessage();
        }
    }
}
