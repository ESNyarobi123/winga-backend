package com.winga.repository;

import com.winga.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    Optional<JobCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<JobCategory> findAllByOrderBySortOrderAsc();
}
