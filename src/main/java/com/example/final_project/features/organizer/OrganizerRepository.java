package com.example.final_project.features.organizer;

import com.example.final_project.domain.Organizer;
import com.example.final_project.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Integer> {
    Optional<Organizer> findByCreatedBy(User user);

    boolean existsByCreatedBy(User user);
}
