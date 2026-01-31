package com.example.final_project.features.event;

import com.example.final_project.domain.Event;
import com.example.final_project.domain.Organizer;
import com.example.final_project.features.event.dto.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    //@EntityGraph(attributePaths = {"category", "organizer"})
    //@EntityGraph(attributePaths = {"category", "organizer"})// ទុកតែ ManyToOne Relationships
    //@Override
    @EntityGraph(attributePaths = {"category", "organizer" ,"ticketTypes"})
    // ហាមដាក់ ticketTypes ឬ imagesEvent ក្នុងនេះ ព្រោះវាធ្វើឱ្យ Response រីកធំខ្លាំង (Cartesian Product)
    Page<Event> findAllByIsDeletedFalse(Pageable pageable);


    Optional<Event> findByUuidAndIsDeletedFalse(String uuid);

    @Query("SELECT e FROM Event e WHERE e.start_date >= :start AND e.start_date <= :end ORDER BY e.start_date ASC")
    List<Event> findEventsForThisWeekByIsDeletedFalse(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Simplified query without the "IS NULL" check in SQL
    @Query("""
    SELECT e FROM Event e
    WHERE e.isDeleted = false
      AND (:category IS NULL OR LOWER(e.category.name) LIKE LOWER(CONCAT('%', :category, '%')))
      AND (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))
""")
    List<Event> findByFilter(
            @Param("category") String category,
            @Param("title") String title
    );



    List<Event> findByOrganizer(Organizer organizer);
}