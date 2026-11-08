package backend.academy.linktracker.scrapper.exc;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import backend.academy.linktracker.scrapper.exc.apiException.ScrapperApiException;
import backend.academy.linktracker.scrapper.model.dto.ApiErrorResponseDto;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages = "backend.academy.linktracker.scrapper")
public class ScrapperApiExceptionHandler {

    @ExceptionHandler(ScrapperApiException.class)
    public ResponseEntity<ApiErrorResponseDto> handleScrapperApiException(ScrapperApiException ex) {
        return error(ex.status(), ex.description(), ex);
    }

     @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        ConstraintViolationException.class,
        MissingRequestHeaderException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponseDto> handleBadRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "Некорректные параметры запроса", ex);
    }
    
    private ResponseEntity<ApiErrorResponseDto> error(HttpStatus status, String description, Exception ex) {
        return ResponseEntity
            .status(status)
            .body(new ApiErrorResponseDto(
                description,
                String.valueOf(status.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                stacktrace(ex)
            ));
    }

    private List<String> stacktrace(Exception ex) {
        return Arrays.stream(ex.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();
    }
}
