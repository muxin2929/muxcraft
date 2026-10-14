package com.muxcraft.config;

import com.muxcraft.security.TurnstileFilter;
import com.muxcraft.security.TurnstileService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
public class TurnstileFilterConfig {

    private final TurnstileService turnstileService;
    private final TurnstileProperties turnstileProperties;

    public TurnstileFilterConfig(TurnstileService turnstileService, TurnstileProperties turnstileProperties) {
        this.turnstileService = turnstileService;
        this.turnstileProperties = turnstileProperties;
    }

    @Bean
    public FilterRegistrationBean<MultipartFilter> multipartFilterRegistration() {
        MultipartFilter filter = new MultipartFilter();
        filter.setMultipartResolverBeanName("multipartResolver");
        FilterRegistrationBean<MultipartFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("multipartFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TurnstileFilter> turnstileFilterRegistration() {
        FilterRegistrationBean<TurnstileFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TurnstileFilter(turnstileService, turnstileProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("turnstileFilter");
        return registration;
    }
}