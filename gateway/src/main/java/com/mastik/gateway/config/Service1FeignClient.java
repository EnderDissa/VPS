package com.mastik.gateway.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SERVICE1")
public interface Service1FeignClient {
    @GetMapping("/data/{id}")
    String getService1Data(@PathVariable("id") String id);
}

