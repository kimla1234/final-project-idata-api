package com.example.final_project.features.user;

import com.example.final_project.domain.Role;
import com.example.final_project.domain.User;
import com.example.final_project.features.user.dto.UserResponse;
import com.example.final_project.features.user.dto.UserUpdateRequest;
import jakarta.transaction.Transactional;
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

        // 🎯 កែ Claim ពី "iss" មក "sub" (Subject) ឬ "email" អាស្រ័យលើ Token របស់បង
        String email = jwt.getClaimAsString("sub");

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with uuid " + uuid + " not found"
                ));

        // ឆែកមើលថា តើអ្នកកែហ្នឹងគឺជាម្ចាស់ Account ឬអត់
        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to update this user"
            );
        }

        if (user.getIsDelete()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User with uuid " + uuid + " not found"
            );
        }

        // 🎯 ១. Map ទិន្នន័យទូទៅ (Name, Address, etc.)
        userMapper.mapFromUserUpdateRequestToUser(userUpdateRequest, user);

        // 🎯 ២. បន្ថែម Logic សម្រាប់ Update រូបភាព (សំខាន់ខ្លាំង!)
        if (userUpdateRequest.profileImage() != null) {
            user.setProfileImage(userUpdateRequest.profileImage());
        }

        if (userUpdateRequest.coverImage() != null) {
            user.setCoverImage(userUpdateRequest.coverImage());
        }

        if (userUpdateRequest.phone() != null) {
            user.setPhone(userUpdateRequest.phone());
        }

        user = userRepository.save(user);

        return userMapper.mapFromUserToUserResponse(user);
    }
    @Override
    @Transactional
    public void followUser(String uuid, Jwt jwt) {
        String currentUserEmail = jwt.getClaimAsString("sub");

        User me = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user"));

        User targetUser = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (me.getUuid().equals(uuid)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        // 🎯 ប្រើ Stream ដើម្បីឆែកតាម UUID បង្ការបញ្ហា Proxy object
        boolean isAlreadyFollowing = targetUser.getFollowers().stream()
                .anyMatch(f -> f.getUuid().equals(me.getUuid()));

        if (isAlreadyFollowing) {
            // បើមានហើយ លុបចេញ
            targetUser.getFollowers().removeIf(f -> f.getUuid().equals(me.getUuid()));
            log.info("User {} unfollowed {}", me.getEmail(), targetUser.getEmail());
        } else {
            // បើអត់ទាន់មាន ថែមចូល
            targetUser.getFollowers().add(me);
            log.info("User {} followed {}", me.getEmail(), targetUser.getEmail());
        }

        // 🎯 ប្រើ saveAndFlush ដើម្បីបង្ខំឱ្យវាសរសេរចូល DB ភ្លាមៗក្នុង Transaction នេះ
        userRepository.saveAndFlush(targetUser);
    }
}
