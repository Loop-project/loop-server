package server.loop.global.common.exception.dto;

import capstone.mju.backend.domain.common.exception.CustomException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
    @JsonProperty("errorCode")
    private final String errorCode;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("detail")
    private final String detail;

    public static ErrorResponseDto res(final CustomException customException){
        String errorCode = customException.getErrorCode().getCode();
        String message = customException.getErrorCode().getMessage();
        String detail = customException.getDetail();
        return new ErrorResponseDto(errorCode,message,detail);
    }

    public static ErrorResponseDto res(final String errorCode, final Exception e){
        return new ErrorResponseDto(errorCode, e.getMessage(), null);
    }

}
