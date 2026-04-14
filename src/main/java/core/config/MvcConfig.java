package core.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import core.interceptor.LoginInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan({ "core.exception", "web.*.controller", "core.interceptor" })
public class MvcConfig implements WebMvcConfigurer {
	
	@Autowired
	private LoginInterceptor loginInterceptor;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("/");
	}
	// 前後端分離架構下，不需要託管viewResolver

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
		messageConverter.setPrettyPrint(true);
		converters.add(messageConverter);
		converters.add(new ByteArrayHttpMessageConverter());
	}

	@Override
	
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(loginInterceptor)
		.addPathPatterns("/api/**")
		.excludePathPatterns("/api/login/**",
				"/api/register/**", "/api/loginCheck", 
				"/api/forgetPassword", "/api/resetPassword", 
				"/api/adminLogin","/api/admin/**","/api/adminLogout");
	}

}
