package com.example.skilltrack.repository;

import com.example.skilltrack.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    @EntityGraph(value = "Course.withModules", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT c FROM Course c WHERE c.deleted = false")
    List<Course> findAllActive();
    
    @EntityGraph(value = "Course.withModules", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT c FROM Course c WHERE c.id = :id AND c.deleted = false")
    Optional<Course> findActiveById(@Param("id") Long id);
    
    List<Course> findAllByDeletedFalse();
    
    Optional<Course> findByIdAndDeletedFalse(Long id);
    
    List<Course> findByInstructorIdAndDeletedFalse(Long instructorId);
}
