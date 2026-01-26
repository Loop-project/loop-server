package server.loop.global.common.exception;


import server.loop.global.common.error.ErrorCode;

public class DtoValidationException extends CustomException {
    public DtoValidationException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}