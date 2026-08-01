package org.microsoft.qintelipass.configs;

import org.microsoft.qintelipass.interceptors.TokenQuotaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 Token 配额拦截器。
 */
@Configuration
public class TokenWebMvcConfig implements WebMvcConfigurer {

    private final TokenQuotaInterceptor tokenQuotaInterceptor;

    public TokenWebMvcConfig(TokenQuotaInterceptor tokenQuotaInterceptor) {
        this.tokenQuotaInterceptor = tokenQuotaInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenQuotaInterceptor)
                .addPathPatterns(
                        "/v1/chat/**",
                        "/api/v1/chat/**",
                        "/api/ai/chat",
                        "/api/ai/chat/**"
                );
    }
}
