package org.theta.framework.cloud;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("remoteServiceConsumerConfig")
@ConfigurationProperties(prefix = "theta.cloud.consumer")
public class RemoteServiceConsumerConfig implements ApplicationContextAware, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RemoteServiceConsumerConfig.class);
	private ApplicationContext context;

	@Autowired
	private RestTemplate restTemplate;

	private Map<String, List<String>> consumerClasses;

	public RemoteServiceConsumerConfig() {
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ConfigurableApplicationContext caContext = (ConfigurableApplicationContext) this.context;
		if (consumerClasses != null) {
			for (String serviceName : consumerClasses.keySet()) {
				List<String> classNames = consumerClasses.get(serviceName);
				Class<?>[] classes = new Class<?>[classNames.size()];
				for (int i = 0; i < classNames.size(); i++) {
					try {
						classes[i] = Class.forName(classNames.get(i));
					} catch (ClassNotFoundException e) {
						logger.error("Remote service bean creation failed.Class : " + classNames.get(i), e);
						System.exit(-1);
					}
				}
				for (Class<?> clazz : classes) {
					Object bean = RemoteServiceInvocationHandler.getRemoteBean(clazz, serviceName, restTemplate);
					String beanName = clazz.getName();
					if (!caContext.containsBean(beanName)) {
						caContext.getBeanFactory().registerSingleton(beanName, bean);
					}
				}
			}
		}
	}

	public Map<String, List<String>> getConsumerClasses() {
		return consumerClasses;
	}

	public void setConsumerClasses(Map<String, List<String>> consumerClasses) {
		this.consumerClasses = consumerClasses;
	}

}
