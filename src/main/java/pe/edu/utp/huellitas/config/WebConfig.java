package pe.edu.utp.huellitas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns(
                        "/dashboard",
                        "/productos/**",
                        "/proveedores/**",
                        "/personal/**"
                )
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/registro",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );
    }
}