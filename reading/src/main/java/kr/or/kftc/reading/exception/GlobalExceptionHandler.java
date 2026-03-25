package kr.or.kftc.reading.exception;

import kr.or.kftc.reading.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(e.getStatus().value())
                .error(e.getStatus().getReasonPhrase())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(e.getStatus()).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("파일 크기가 제한을 초과했습니다.")
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse error = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("서버 내부 오류가 발생했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
