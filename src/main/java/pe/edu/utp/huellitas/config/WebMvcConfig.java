package pe.edu.utp.huellitas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.edu.utp.huellitas.security.ForcedPasswordChangeInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ForcedPasswordChangeInterceptor forcedPasswordChangeInterceptor;

    public WebMvcConfig(ForcedPasswordChangeInterceptor forcedPasswordChangeInterceptor) {
        this.forcedPasswordChangeInterceptor = forcedPasswordChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(forcedPasswordChangeInterceptor);
    }
}
