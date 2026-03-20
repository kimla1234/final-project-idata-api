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
            @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles"),
            @Mapping(target = "followersCount", source = "user", qualifiedByName = "countFollowers"),
            // បើកវាវិញទាំងអស់ (Uncomment)
            @Mapping(target = "address", source = "address"), // បន្ថែម address បើក្នុង Response មាន
            @Mapping(target = "coverImage", source = "coverImage")
    })
    UserResponse mapFromUserToUserResponse(User user);

    // បម្លែង List<Role> ទៅជា List<String>
    @Named("mapRoles")
    default List<String> mapRoles(List<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Role::getName)
                .toList();
    }

    // រាប់ចំនួន Followers ចេញពី List<User> followers ក្នុង Entity
    @Named("countFollowers")
    default Integer countFollowers(User user) {
        if (user.getFollowers() == null) {
            return 0;
        }
        return user.getFollowers().size();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mapFromUserUpdateRequestToUser(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}