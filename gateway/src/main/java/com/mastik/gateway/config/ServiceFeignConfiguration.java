package com.mastik.gateway.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class ServiceFeignConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}