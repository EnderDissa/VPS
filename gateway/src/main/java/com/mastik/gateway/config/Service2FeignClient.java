package com.mastik.gateway.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SERVICE2")
public interface Service2FeignClient {
    @GetMapping("/data/{id}")
    String getService2Data(@PathVariable("id") String id);
}
