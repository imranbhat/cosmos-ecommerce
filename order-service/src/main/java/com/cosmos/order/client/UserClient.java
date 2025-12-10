package com.cosmos.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url:http://user-service:8080}")
public interface UserClient {

    @GetMapping("/users/{id}")
    Object getUserById(@PathVariable("id") Long id);
}
