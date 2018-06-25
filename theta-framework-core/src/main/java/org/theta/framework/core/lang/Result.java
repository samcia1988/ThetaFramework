package org.theta.framework.core.lang;

import java.io.Serializable;

public class Result implements Serializable {
	private static final long serialVersionUID = 4425372968541587317L;
	protected boolean success = true;
	protected String errorCode = "";
	protected String message = "";
	protected String[] args;

	public Result copy(Result result) {
		if (result != null) {
			this.success = result.success;
			this.errorCode = result.errorCode;
			this.message = result.message;
		}
		return this;
	}

	public void fail() {
		this.success = false;
	}

	public void fail(String errorCode) {
		this.success = false;
		this.errorCode = errorCode;
	}

	public void fail(String errorCode, String message) {
		this.success = false;
		this.errorCode = errorCode;
		this.message = message;
	}

	public void fail(String errorCode, String message, String[] args) {
		this.success = false;
		this.errorCode = errorCode;
		this.message = message;
		this.args = args;
	}

	public boolean isSuccess() {
		return this.success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String[] getArgs() {
		return this.args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
}