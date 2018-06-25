package org.theta.framework.core.trace;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.MDC;

public class TraceLog {
	private Logger logger = null;
	private String method = null;
	private long threshold = 0L;
	private String invokeNo = null;
	private String exType = "N";
	private String errorCode = "success";
	private String beyondThd = "N";
	private long beginTime;
	private long endTime;
	private StringBuilder message = new StringBuilder();
	public static final String MDC_INVOKE_NO = "invokeNo";

	public TraceLog(Logger logger, String method, long threshold) {
		this.logger = logger;
		this.method = method;
		this.threshold = threshold;
		this.invokeNo = MDC.get(MDC_INVOKE_NO);
		if (this.invokeNo == null)
			MDC.put(MDC_INVOKE_NO, UUID.randomUUID().toString().replace("-", ""));
	}

	public void begin() {
		this.beginTime = System.currentTimeMillis();
	}

	public void end() {
		if (this.logger.isInfoEnabled()) {
			this.endTime = System.currentTimeMillis();
			long runTime = this.endTime - this.beginTime;
			if ((this.threshold > 0L) && (runTime > this.threshold))
				this.beyondThd = "Y";
			this.message.append("ME:").append(this.method).append("|RT:").append(runTime).append("|BT:")
					.append(this.beyondThd).append("|ET:").append(this.exType).append("|EC:").append(this.errorCode);
		}
	}

	public void reset(String method, long threshold) {
		this.method = method;
		this.threshold = threshold;
		this.exType = "N";
		this.beyondThd = "N";
	}

	public void setExType(String exType) {
		this.exType = exType;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void log() {
		try {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(this.message.toString());
			}
		} finally {
			if (this.invokeNo == null)
				MDC.remove(MDC_INVOKE_NO);
		}
	}

	@Override
	public String toString() {
		return this.message.toString();
	}
}
