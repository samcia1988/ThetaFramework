package org.theta.framework.cloud;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.web.client.RestTemplate;

public class RemoteServiceInvocationHandler implements InvocationHandler {

	private RestTemplate restTemplate;

	private String serviceName;

	private String extServiceName;

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setExtServiceName(String extServiceName) {
		this.extServiceName = extServiceName;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		if (methodName.equals("hashCode") && (args == null || args.length == 0)) {
			return this.hashCode();
		}
		if (methodName.equals("toString") && (args == null || args.length == 0)) {
			return obj.getClass().getName() + "@remote";
		}
		Class<?> resultType = method.getReturnType();
		Object result = this.restTemplate.postForObject(
				"http://" + serviceName + "/remoteInvoke/" + extServiceName + "/" + methodName, args, resultType);
		return result;
	}

	public Object getInstance(Class<?> serviceClass) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(serviceClass);
		enhancer.setCallback(this);
		return enhancer.create();
	}

	public static Object getRemoteBean(Class<?> serviceClass, String serviceName, RestTemplate restTemplate) {
		RemoteServiceInvocationHandler handler = new RemoteServiceInvocationHandler();
		handler.setServiceName(serviceName);
		handler.setExtServiceName(serviceClass.getName());
		handler.setRestTemplate(restTemplate);
		Object proxy = handler.getInstance(serviceClass);
		return proxy;
	}

}
