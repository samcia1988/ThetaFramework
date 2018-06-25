package org.theta.framework.core.lang;

public class AppException extends RuntimeException {
	private static final long serialVersionUID = -101473286311108767L;
	protected String errorCode;
	protected String[] args;

	public AppException(String errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public AppException(Result result) {
		super(result.getMessage());
		this.errorCode = result.getErrorCode();
	}

	public AppException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public AppException(String errorCode, String message, String[] args) {
		super(message);
		this.errorCode = errorCode;
		this.args = args;
	}

	public AppException(String errorCode, String[] args) {
		this.errorCode = errorCode;
		this.args = args;
	}

	public AppException(String errorCode, Throwable cause) {
		super(errorCode, cause);
		this.errorCode = errorCode;
	}

	public AppException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String[] getArgs() {
		return this.args;
	}

	public Throwable fillInStackTrace() {
		return this;
	}
}
