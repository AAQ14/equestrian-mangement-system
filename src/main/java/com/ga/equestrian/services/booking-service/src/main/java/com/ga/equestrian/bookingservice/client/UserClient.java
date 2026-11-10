package com.ga.equestrian.bookingservice.client;

import com.ga.equestrian.bookingservice.dto.response.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserSummary getUser(@PathVariable Long id);
}
