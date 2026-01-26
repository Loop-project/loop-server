package server.loop.global.common.exception;


import capstone.mju.backend.domain.common.error.ErrorCode;

public class NotFoundException extends CustomException {
    public NotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

