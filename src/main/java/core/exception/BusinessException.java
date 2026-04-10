package core.exception;

import org.springframework.http.HttpStatus;




public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus status; // 新增狀態碼欄位

	public BusinessException(String message) {
		this(message, HttpStatus.BAD_REQUEST); // 預設 400
						
	}

	public BusinessException(String message, HttpStatus status) {
		super(message);
		this.status = status;
		
	}

	public HttpStatus getStatus() {
		return status;
	}
}
