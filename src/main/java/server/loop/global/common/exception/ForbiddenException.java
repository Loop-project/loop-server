package server.loop.global.common.exception;


import capstone.mju.backend.domain.common.error.ErrorCode;

public class ForbiddenException extends CustomException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}