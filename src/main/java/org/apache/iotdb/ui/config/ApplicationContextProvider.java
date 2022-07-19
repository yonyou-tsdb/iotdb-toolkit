package org.apache.iotdb.ui.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
	private static ApplicationContext context = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (context == null) {
			context = applicationContext;
		}
	}

	// 閫氳繃name鑾峰彇 Bean.
	public static Object getBean(String name) {
		return context.getBean(name);

	}

	// 閫氳繃class鑾峰彇Bean.
	public static <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	// 閫氳繃name,浠ュ強Clazz杩斿洖鎸囧畾鐨凚ean
	public static <T> T getBean(String name, Class<T> clazz) {
		return context.getBean(name, clazz);
	}

	// 鑾峰彇鐜鍙橀噺
	public static <T> T getEnvironmentProperty(String key, Class<T> targetClass, T defaultValue) {
		if (key == null || targetClass == null) {
			throw new NullPointerException();
		}

		T value = null;
		if (context != null) {
			value = context.getEnvironment().getProperty(key, targetClass, defaultValue);
		}
		return value;
	}
}
