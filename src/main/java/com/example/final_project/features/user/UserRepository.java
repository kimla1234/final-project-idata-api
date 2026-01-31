package com.example.final_project.features.user;

import com.example.final_project.domain.Role;
import com.example.final_project.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(String uuid);
    // Use "In" to accept a List/Collection
    Page<User> findAllByRolesInAndIsDeleteFalse(List<Role> roles, Pageable pageable);
    Optional<User> findByEmailAndIsBlockFalseAndIsDeleteFalse(String email);


}
