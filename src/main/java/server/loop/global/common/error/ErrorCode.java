package server.loop.global.common.error;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 유효하지 않는 Request
    INVALID_BODY("4000", "유효하지 않은 Body 형식입니다. :"),
    INVALID_EMAIL("4001", "유효하지 않은 이메일 형식입니다."),
    INVALID_PASSWORD("4002", "유효하지 않는 비밀번호 형식입니다."),
    INVALID_NAME("4003", "유효하지 않는 이름 형식입니다."),
    INVALID_SEARCH_KEYWORD("4004", "검색 키워드가 유효하지 않습니다."),

    // 401 Unauthorized
    UNAUTHORIZED_USER("4010", "로그인 정보가 틀렸습니다."),
    INVALID_TOKEN("4011", "잘못된 토큰입니다."),

    // 403 Forbidden
    FORBIDDEN_USER("4030", "접근 권한이 없습니다."),
    ALREADY_SCRAPPED("4031", "이미 스크랩 된 내용입니다."),

    // 404 Not Found
    POST_NOT_FOUND("4040", "해당 포스트를 찾을 수 없습니다."),
    USER_NOT_FOUND("4041", "해당 유저를 찾을 수 없습니다. "),
    CHAT_ROOM_NOT_FOUND("4042", "해당 채팅방을 찾을 수 없습니다."),

    // 405 Using wrong HTTP Method
    WRONG_METHOD("4050", "허용되지 않은 메소드 입니다."),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS("4090", "해당 이메일 사용자가 이미 존재합니다."),
    CONFLICT("4091", "요청 충돌 발생"),
    ALREADY_EXISTS("4092", "이미 존재합니다."),

    // 500 Server
    UNKNOWN_SERVER_ERROR("5000", "알 수 없는 서버 내부 오류입니다."),
    UNKNOWN_DB_ERROR("5001", "JPA 오류가 발생하였습니다."),
    INTERNAL_SERVER_ERROR("5002", "서버 내부 오류가 발생했습니다."),


    // 502 - Bad Gateway

    // 900 Request Body
    NOT_NULL("9001", "필수값이 누락되었습니다."),
    NOT_BLANK("9001", "필수값이 빈 값이거나 공백으로 되어있습니다."),
    REGEX("9002", "형식에 맞지 않습니다. :"),
    LENGTH("9003", "길이가 유효하지 않습니다. :"),

    // S3 관련 오류
    EMPTY_FILE_EXCEPTION("6000", "파일이 비어 있습니다."),
    IO_EXCEPTION_ON_IMAGE_UPLOAD("6001", "이미지 업로드 중 IO 예외가 발생했습니다."),
    NO_FILE_EXTENSION("6002", "파일에 확장자가 없습니다."),
    INVALID_FILE_EXTENSION("6003", "지원하지 않는 파일 확장자입니다."),
    PUT_OBJECT_EXCEPTION("6004", "S3 업로드(PutObject)에 실패했습니다."),
    IO_EXCEPTION_ON_IMAGE_DELETE("6005", "이미지 삭제 중 IO 예외가 발생했습니다.");


    private final String code;
    private final String message;

    public static ErrorCode resolveValidationErrorCode(String code) {
        return switch (code) {
            case "NotNull" -> NOT_NULL;
            case "NotBlank" -> NOT_BLANK;
            case "Pattern" -> REGEX;
            case "Length" -> LENGTH;
            default -> throw new IllegalArgumentException("Unexpected value: " + code);
        };
    }
}
