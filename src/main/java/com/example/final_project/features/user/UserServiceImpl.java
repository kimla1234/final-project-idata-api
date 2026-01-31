package com.example.final_project.features.user;

import com.example.final_project.domain.Role;
import com.example.final_project.domain.User;
import com.example.final_project.features.user.dto.UserResponse;
import com.example.final_project.features.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final com.example.final_project.mapper.userMapper userMapper;


    @Override
    public Page<UserResponse> findAllUser(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Role not found"
                ));
        Page<User> user = userRepository.findAllByRolesInAndIsDeleteFalse(List.of(role),pageRequest);
        return user.map(userMapper::mapFromUserToUserResponse);
    }


    @Override
    public UserResponse findOwnProfile(Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }

        String email = jwt.getClaimAsString("iss");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return userMapper.mapFromUserToUserResponse(user);
    }

    @Override
    public UserResponse updateUserByUuid(String uuid, UserUpdateRequest userUpdateRequest, Jwt jwt) {

        String email = jwt.getClaimAsString("iss");


        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with uuid " + uuid + " not found"
                ));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update this user"
            );
        }

        if (user.getIsDelete().equals(true)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User with uuid " + uuid + " not found"
            );
        }

        userMapper.mapFromUserUpdateRequestToUser(userUpdateRequest, user);
        if (userUpdateRequest.phone() != null) {
            user.setPhone(userUpdateRequest.phone());
        }
        user = userRepository.save(user);

        return userMapper.mapFromUserToUserResponse(user);
    }
}
