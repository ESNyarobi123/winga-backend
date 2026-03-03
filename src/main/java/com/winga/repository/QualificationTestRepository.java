package com.winga.repository;

import com.winga.entity.QualificationTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QualificationTestRepository extends JpaRepository<QualificationTest, Long> {

    List<QualificationTest> findByIsActiveTrueOrderBySortOrderAsc();

    List<QualificationTest> findAllByOrderBySortOrderAsc();

    Optional<QualificationTest> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
