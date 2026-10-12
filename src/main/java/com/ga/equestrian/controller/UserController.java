package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.UpdateUserRequest;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.model.enums.UserStatus;
import com.ga.equestrian.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * Updates the profile of the signed-in user, including the profile picture.
     * Returns 200 on success, 400 if the data or the file is invalid,
     * 401 or 403 if the token is missing or invalid, and 404 if the user does not exist.
     *
     * @param authentication the signed-in user, set by the JWT filter.
     * @param request the fields to change, sent as form data.
     * @return the updated user.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateOwnProfile(Authentication authentication, @Valid @ModelAttribute UpdateUserRequest request){
        UserResponse response = userService.updateOwnProfile(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * Updates the profile of any user. Admins only.
     * Returns 200 on success, 400 if the data or the file is invalid, 403 if the caller is not an admin,
     * and 404 if the user does not exist.
     *
     * @param id the id of the user to update.
     * @param authentication the signed-in admin, set by the JWT filter.
     * @param request the fields to change, sent as form data.
     * @return the updated user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, Authentication authentication, @Valid @ModelAttribute UpdateUserRequest request){
        UserResponse response = userService.updateUser(authentication.getName(), id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns all users, optionally filtered by status. Admins only.
     * Returns 200 on success, 400 if the status value is invalid, and 403 if the caller is not an admin.
     *
     * @param userStatus the status to filter by, or null.
     * @return the list of users.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(@RequestParam(required = false) UserStatus userStatus){
        List<UserResponse> users = userService.getAllUsers(userStatus);
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    /**
     * Returns one user by id. Admins only.
     * Returns 200 on success, 403 if the caller is not an admin, and 404 if the user does not exist.
     *
     * @param id the user id.
     * @return the user.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


    /**
     * Deactivates a user (soft delete). Admins only.
     * Returns 200 on success, 403 if the caller is not an admin, 404 if the user does not exist,
     * and 422 if the admin targets themselves or the user is already inactive.
     *
     * @param id the id of the user to deactivate.
     * @param authentication the signed-in admin, set by the JWT filter.
     * @return the deactivated user.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id, Authentication authentication){
        UserResponse response = userService.deactivateUser(authentication.getName(), id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
