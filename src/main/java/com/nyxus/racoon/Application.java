package com.nyxus.racoon;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Application implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		
		ctx.register(SpringConfiguration.class);
		
		DispatcherServlet servlet = new DispatcherServlet(ctx);
		ServletRegistration.Dynamic registro = servletContext.addServlet("app", servlet);
		
		registro.setLoadOnStartup(1);
		registro.addMapping("/");
		registro.setAsyncSupported(true);
	}	
}
