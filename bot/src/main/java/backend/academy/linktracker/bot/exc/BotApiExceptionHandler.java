package backend.academy.linktracker.bot.exc;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import backend.academy.linktracker.bot.model.dto.ApiErrorResponseDto;

@RestControllerAdvice(basePackages = "backend.academy.linktracker.client")
public class BotApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return badRequest(HttpStatus.BAD_REQUEST, "Некорректные параметры запроса", ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return badRequest(HttpStatus.BAD_REQUEST, "Некорректные параметры запроса", ex);
    }

    private ResponseEntity<ApiErrorResponseDto> badRequest(HttpStatus status, String description, Exception ex) {
        return ResponseEntity.badRequest().body(
            new ApiErrorResponseDto(
                description,
                String.valueOf(status.value()),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toList()
            )
        );
    }
}