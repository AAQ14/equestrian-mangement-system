package com.ga.equestrian.bookingservice.client;

import com.ga.equestrian.bookingservice.dto.response.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/api/users/{id}/summary")
    UserSummary getUser(@PathVariable Long id);

    @PutMapping("/api/users/{id}/role")
    UserSummary updateUser(@PathVariable Long id, @RequestParam("role") String role);
}
