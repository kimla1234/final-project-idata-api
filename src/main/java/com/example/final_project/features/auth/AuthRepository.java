package com.example.final_project.features.auth;

import com.example.final_project.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailAndVerificationCodeAndIsDeleteFalse(String email, String verificationCode);

}
