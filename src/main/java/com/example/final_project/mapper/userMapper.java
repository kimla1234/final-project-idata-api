package com.example.final_project.mapper;

import com.example.final_project.domain.Role;
import com.example.final_project.domain.User;
import com.example.final_project.features.user.dto.UserResponse;
import com.example.final_project.features.user.dto.UserUpdateRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface userMapper {
    @Mappings({
            @Mapping(target = "roles", source = "roles"),
            @Mapping(target = "dob", ignore = true),
            @Mapping(target = "country", ignore = true),
            @Mapping(target = "city", ignore = true)
    })
    UserResponse mapFromUserToUserResponse(User user);
    // Add this helper method:
    default List<String> mapRoles(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName) // Or getRoleName() depending on your Role entity
                .toList();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User mapFromUserUpdateRequestToUser(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}
