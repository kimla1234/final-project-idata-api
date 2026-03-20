package com.example.final_project.features.user;

import com.example.final_project.features.user.dto.UserResponse;
import com.example.final_project.features.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
@Hidden
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    public Page<UserResponse> findAllUsers (@RequestParam(required = false,defaultValue = "0") int page ,
                                            @RequestParam(required = false ,defaultValue = "10") int size){
        return userService.findAllUser(page, size);
    }

    // Add the newline character directly to the mapping array
    @GetMapping("/me")
    public UserResponse findOwnProfile(@AuthenticationPrincipal Jwt jwt) {
        return userService.findOwnProfile(jwt);
    }

    @PatchMapping("/{uuid}")
    public UserResponse updateUser(@PathVariable String uuid,
                                   @RequestBody UserUpdateRequest userUpdateRequest,
                                   @AuthenticationPrincipal Jwt jwt){
        return userService.updateUserByUuid(uuid, userUpdateRequest, jwt);
    }

    // --- 🎯 បន្ថែមមុខងារ Follow ត្រង់នេះ ---
    @PostMapping("/follow/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Follow or Unfollow a user")
    public void followUser(@PathVariable String uuid, @AuthenticationPrincipal Jwt jwt) {
        userService.followUser(uuid, jwt);
    }
}
