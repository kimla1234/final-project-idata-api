package com.example.final_project.features.token;

import com.example.final_project.domain.Token;
import com.example.final_project.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    Token findByUser(User user);

    @Modifying
    void deleteByUser(User user);
}
