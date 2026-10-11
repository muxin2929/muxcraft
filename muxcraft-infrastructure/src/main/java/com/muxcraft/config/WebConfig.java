package com.muxcraft.config;

import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册认证/管理员拦截器、静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final FileProperties fileProperties;

    public WebConfig(AuthInterceptor authInterceptor, FileProperties fileProperties) {
        this.authInterceptor = authInterceptor;
        this.fileProperties = fileProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/register",
                        "/auth/login",
                        "/error",
                        "/uploads/**",
                        "/ws/**"
                );

        registry.addInterceptor(adminInterceptor())
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localPath = fileProperties.getLocalPath();
        if (!localPath.endsWith("/")) {
            localPath = localPath + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + localPath);
    }

    private HandlerInterceptor adminInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                LoginUser loginUser = AuthInterceptor.currentUser();
                if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
                    throw new BizException(ResultCode.FORBIDDEN, "无权限");
                }
                return true;
            }
        };
    }
}