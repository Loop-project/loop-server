package server.loop.global.common.exception.controller;

import server.loop.global.common.error.ErrorCode;
import server.loop.global.common.exception.CustomException;
import server.loop.global.common.exception.DtoValidationException;
import server.loop.global.common.exception.dto.ErrorResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException customException){
        writeLog(customException);
        HttpStatus httpStatus = this.resolveHttpStatus(customException);
        return new ResponseEntity<>(ErrorResponseDto.res(customException), httpStatus);
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponseDto> handleCustomException(MethodArgumentNotValidException methodArgumentNotValidException){
        FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
        if(fieldError == null){
            return new ResponseEntity<>(ErrorResponseDto.res(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    methodArgumentNotValidException), HttpStatus.BAD_REQUEST);
        }
        ErrorCode validationErrorCode = ErrorCode.resolveValidationErrorCode(fieldError.getCode());
        String detail = fieldError.getDefaultMessage();
        DtoValidationException dtoValidationException = new DtoValidationException(validationErrorCode, detail);
        this.writeLog(dtoValidationException);
        return new ResponseEntity<>(ErrorResponseDto.res(dtoValidationException),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFoundException(EntityNotFoundException entityNotFoundException){
        writeLog(entityNotFoundException);
        return new ResponseEntity<>(ErrorResponseDto.res(String.valueOf(HttpStatus.NOT_FOUND.value()),entityNotFoundException), HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleExceptioon(Exception exception){
        this.writeLog(exception);
        return new ResponseEntity<>(
                ErrorResponseDto.res(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), exception),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private void writeLog(CustomException customException){
        String exceptionName = customException.getClass().getSimpleName();
        ErrorCode errorCode = customException.getErrorCode();
        String detail = customException.getDetail();
        log.error("[{}]{}:{}", exceptionName,errorCode.getMessage(), detail);
    }

    private void writeLog(Exception exception){
        String exceptionName = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        log.error("[{}]:{}", exceptionName, message);
    }

    private HttpStatus resolveHttpStatus(CustomException customException){
        String errorCode = customException.getErrorCode().getCode();
        if (errorCode == null || errorCode.length() < 3) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try {
            HttpStatus resolved = HttpStatus.resolve(Integer.parseInt(errorCode.substring(0, 3)));
            if (resolved != null) {
                return resolved;
            }
        } catch (NumberFormatException ignored) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Non-HTTP custom code fallback
        if (errorCode.startsWith("9")) {
            return HttpStatus.BAD_REQUEST;
        }
        if (errorCode.startsWith("6")) {
            return switch (customException.getErrorCode()) {
                case EMPTY_FILE_EXCEPTION, NO_FILE_EXTENSION, INVALID_FILE_EXTENSION -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
