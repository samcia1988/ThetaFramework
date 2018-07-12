package org.theta.framework.cloud;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.theta.framework.core.lang.AppException;
import org.theta.framework.core.lang.ErrorCode;

import com.alibaba.fastjson.JSON;

@RestController
@ConfigurationProperties(prefix = "theta.cloud.producer")
public class RemoteServiceProducerController implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(RemoteServiceProducerController.class);

	private Class<?>[] producerClasses;
	private ApplicationContext context;

	public static RemoteServiceProducerController getInstance(Class<?>[] producerClasses) {
		RemoteServiceProducerController rpc = new RemoteServiceProducerController();
		rpc.setProducerClasses(producerClasses);
		return rpc;
	}

	public void setProducerClasses(Class<?>[] classes) {
		this.producerClasses = classes;
	}

	@RequestMapping(value = "/remoteInvoke/{extServiceName}/{methodName}", method = RequestMethod.POST)
	public Object remoteInvoke(@PathVariable String extServiceName, @PathVariable String methodName,
			@RequestBody Object[] args) {
		boolean inList = false;
		if (producerClasses != null) {
			for (Class<?> producerClass : producerClasses) {
				if (StringUtils.equals(producerClass.getName().trim(), extServiceName.trim())) {
					inList = true;
					break;
				}
			}
			if (!inList)
				throw new AppException("Error.System", "ExtService's name is not in producer's list.");
		}
		logger.info("Remote invoke. Interface : {},Method : {}", extServiceName, methodName);

		Class<?> beanClass;
		try {
			beanClass = Class.forName(extServiceName);
		} catch (ClassNotFoundException e) {
			throw new AppException(ErrorCode.ERROR_SYSTEM, e);
		}
		Object bean = context.getBean(beanClass);

		Method[] methods = beanClass.getDeclaredMethods();
		long dup = 0;
		Method method = null;
		for (Method m : methods) {
			if (StringUtils.equals(m.getName(), methodName)) {
				dup++;
				method = m;
			}
		}
		if (dup > 1)
			throw new AppException(ErrorCode.ERROR_SYSTEM,
					"Duplicated method names are not allowed in Theta Sping Cloud standard.");
		else if (dup < 1)
			throw new AppException(ErrorCode.ERROR_SYSTEM, "No such method in the invoked bean.");
		Object result;
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes == null || parameterTypes.length == 0)
				result = method.invoke(bean);
			else {
				for (int i = 0; i < args.length; i++) {
					Object remoteParam = args[i];
					Class<?> type = parameterTypes[i];
					Object localParam = null;
					localParam = JSON.parseObject(JSON.toJSONString(remoteParam), type);
					args[i] = localParam;
				}
				result = method.invoke(bean, args);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new AppException(ErrorCode.ERROR_SYSTEM, e);
		}
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
