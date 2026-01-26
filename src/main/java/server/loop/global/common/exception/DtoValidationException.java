package server.loop.global.common.exception;


import capstone.mju.backend.domain.common.error.ErrorCode;

public class DtoValidationException extends CustomException {
    public DtoValidationException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}