package com.t1.achievements.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 404: нет подходящего хэндлера/маппинга
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<StatusResponse> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StatusResponse("error", "Ресурс не найден: " + ex.getRequestURL()));
    }

    // --- 404: отсутствующая path‑переменная (например, /achievements/user без {userId})
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<StatusResponse> handleMissingPathVar(MissingPathVariableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StatusResponse("error", "Параметр пути '" + ex.getVariableName() + "' обязателен"));
    }

    // --- 404: неверный формат path/query параметра (в т.ч. "" → UUID)
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            TypeMismatchException.class
    })
    public ResponseEntity<StatusResponse> handleTypeMismatch(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StatusResponse("error", "Некорректный идентификатор ресурса"));
    }

    // --- 404: доменная «нет такого ресурса»
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<StatusResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StatusResponse("error", ex.getMessage()));
    }

    // --- 400: прочая валидация запроса (оставим Bad Request)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StatusResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StatusResponse("error", ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StatusResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StatusResponse("error", "Параметр '" + ex.getParameterName() + "' обязателен"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StatusResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst().map(v -> v.getMessage()).orElse("Некорректные параметры запроса");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StatusResponse("error", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StatusResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StatusResponse("error", msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StatusResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StatusResponse("error", "Некорректное тело запроса"));
    }

    // --- на всякий случай: стандартный 404, если кто-то бросит NoSuchElementException
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<StatusResponse> handleNoSuchElement(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new StatusResponse("error", ex.getMessage()));
    }

    // --- общий 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StatusResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StatusResponse("error", "Внутренняя ошибка сервера"));
    }
}
