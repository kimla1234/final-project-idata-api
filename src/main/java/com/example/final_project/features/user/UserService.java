package com.example.final_project.features.user;

import com.example.final_project.features.user.dto.UserResponse;
import com.example.final_project.features.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService   {
    Page<UserResponse> findAllUser(int page, int size);
    UserResponse findOwnProfile(Jwt jwt);
    UserResponse updateUserByUuid(String uuid, UserUpdateRequest userUpdateRequest, Jwt jwt);

}
