package com.uwallet.pay.main.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author: Strong
 * @date: 2019-07-2
 * @description: 拦截器
 */
@Configuration
public class MyMvcConfig extends WebMvcConfigurationSupport {

	@Bean
	public Interceptor getInterceptor() {
		return new Interceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(getInterceptor()).addPathPatterns("/**")
				.excludePathPatterns("/doc.html","/error",
						"/webjars/**","/swagger-resources",
						"/editorUpload");

	}

	/**
     * 发现如果继承了WebMvcConfigurationSupport，则在yml中配置的相关内容会失效。
     * 需要重新指定静态资源
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("doc.html")
        .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/templates/");
        super.addResourceHandlers(registry);
    }


}
