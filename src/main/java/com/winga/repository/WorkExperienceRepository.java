package com.winga.repository;

import com.winga.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {

    List<WorkExperience> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
