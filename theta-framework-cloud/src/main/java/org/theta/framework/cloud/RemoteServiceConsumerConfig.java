package org.theta.framework.cloud;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConfigurationProperties(prefix = "theta.cloud.consumer")
public class RemoteServiceConsumerConfig implements ApplicationContextAware {

	private ApplicationContext context;

	@Autowired
	private RestTemplate restTemplate;

	private Map<String, Class<?>[]> consumerClasses;

	public RemoteServiceConsumerConfig() {
		System.out.println(1);
	}

	@Bean
//	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
		ConfigurableApplicationContext caContext = (ConfigurableApplicationContext) this.context;
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) caContext.getBeanFactory();
		if (consumerClasses != null) {
			for (String serviceName : consumerClasses.keySet()) {
				Class<?>[] classes = consumerClasses.get(serviceName);
				for (Class<?> clazz : classes) {
					Object bean = RemoteServiceInvocationHandler.getRemoteBean(clazz, serviceName, restTemplate);
					BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz.getName())
							.getBeanDefinition();
					registry.registerBeanDefinition(clazz.getName(), beanDefinition);
				}
			}
		}
	}

}
