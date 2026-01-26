package server.loop.global.common.exception;


import server.loop.global.common.error.ErrorCode;

public class ConflictException extends CustomException {

    public ConflictException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}