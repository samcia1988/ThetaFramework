package org.theta.framework.core.trace;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import org.theta.framework.core.lang.AppException;
import org.theta.framework.core.lang.ErrorCode;
import org.theta.framework.core.lang.Result;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Aspect
@Order(-99)
@ConfigurationProperties(prefix = "theta.service-trace")
public class ServiceTraceInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(ServiceTraceInterceptor.class);
	private long threshold = 1000L;
	private List<String> filterParams = null;
	private String[] filterArgs;
	private String[] filterSuffix;

	public static ServiceTraceInterceptor getInstance(long threshold, String[] filterArgs) {
		ServiceTraceInterceptor sti = new ServiceTraceInterceptor();
		sti.setThreshold(threshold);
		sti.setFilterArgs(filterArgs);
		return sti;
	}

	public ServiceTraceInterceptor() {

	}

	private ValueFilter nameFilter = new ValueFilter() {
		public Object process(Object source, String name, Object value) {
			Object result = value;
			if ((ServiceTraceInterceptor.this.filterArgs == null) || (value == null)) {
				return result;
			}
			name = name.toUpperCase();
			try {
				for (String filterArg : ServiceTraceInterceptor.this.filterSuffix) {
					if ((name.endsWith(filterArg)) && (ClassUtils.isAssignableValue(String.class, value))) {
						return ((String) value).length() + "L";
					}
				}
				for (String filterArg : ServiceTraceInterceptor.this.filterArgs) {
					if ((name.equals(filterArg)) && (ClassUtils.isAssignableValue(String.class, value))) {
						result = ((String) value).length() + "L";
						break;
					}
				}
			} catch (Throwable t) {
				ServiceTraceInterceptor.logger.error("FastJSON filter exception.", t);
			}
			return result;
		}
	};

	@Around(value = "within(@org.theta.framework.core.trace.ServiceTrace *)")
	public Object invoke(ProceedingJoinPoint pjp) throws Throwable {
		if (this.filterParams != null && filterArgs == null) {
			this.setFilterArgs(filterParams.toArray(new String[0]));
		}
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		Object target = pjp.getThis();
		Object result = null;

		String methodName = target.getClass().getSimpleName() + "." + method.getName();
		TraceLog traceLog = new TraceLog(logger, methodName, this.threshold);

		if (logger.isDebugEnabled())
			logger.debug("ME:{}|Begin|ARGS:{}", methodName,
					JSON.toJSONString(pjp.getArgs(), this.nameFilter, new SerializerFeature[0]));
		try {
			traceLog.begin();
			result = pjp.proceed(pjp.getArgs());
			if (ClassUtils.isAssignable(Result.class, method.getReturnType())) {
				Result newResult = (Result) result;
				if (!newResult.isSuccess()) {
					traceLog.setExType("A");
					traceLog.setErrorCode(newResult.getErrorCode());
				}
			}
		} catch (AppException appEx) {
			traceLog.setExType("A");
			traceLog.setErrorCode(appEx.getErrorCode());
			result = handleException(method, appEx, appEx.getErrorCode(), appEx.getMessage(), appEx.getArgs());
		} catch (Throwable t) {
			traceLog.setExType("T");
			traceLog.setErrorCode(ErrorCode.ERROR_SYSTEM);
			logger.error("Service interceptor invoke " + methodName + " error!", t);
			result = handleException(method, t, ErrorCode.ERROR_SYSTEM, "", null);
		} finally {
			traceLog.end();
			if (logger.isDebugEnabled()) {
				logger.debug("ME:{}|End|Result:{}", methodName,
						JSON.toJSONString(result, this.nameFilter, new SerializerFeature[0]));
			}
			traceLog.log();
		}
		return result;
	}

	private Object handleException(Method method, Throwable t, String errorCode, String errorMessage, String args[]) {
		Object result = null;
		if (ClassUtils.isAssignable(Result.class, method.getReturnType())) {
			result = BeanUtils.instantiateClass(method.getReturnType());
			((Result) result).fail(errorCode, errorMessage, args);

			return result;
		}
		throw new AppException(errorCode, errorMessage);
	}

	public void setThreshold(long threshold) {
		this.threshold = threshold;
	}

	public void setFilterArgs(String[] filterArgs) {
		if (filterArgs == null)
			filterArgs = new String[0];
		List<String> filterArgList = new ArrayList<String>();
		List<String> filterSuffixList = new ArrayList<String>();
		for (String filterArg : filterArgs) {
			if (!StringUtils.isEmpty(filterArg)) {
				if (filterArg.indexOf("*") == 0) {
					filterSuffixList.add(filterArg.toUpperCase().substring(1));
				} else {
					filterArgList.add(filterArg.toUpperCase());
				}
			}
		}
		this.filterArgs = ((String[]) filterArgList.toArray(new String[0]));
		this.filterSuffix = ((String[]) filterSuffixList.toArray(new String[0]));
	}

}
