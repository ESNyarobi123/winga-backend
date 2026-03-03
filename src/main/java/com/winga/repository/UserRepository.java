package com.winga.repository;

import com.winga.entity.User;
import com.winga.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(Role role);

    Page<User> findByRoleAndIsActiveTrueOrderByCreatedAtDesc(Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.headline) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.country) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.workType) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:employmentType IS NULL OR :employmentType = '' OR LOWER(COALESCE(u.workType, '')) LIKE LOWER(CONCAT('%', :employmentType, '%'))) " +
           "AND (:language IS NULL OR :language = '' OR LOWER(COALESCE(u.languages, '')) LIKE LOWER(CONCAT('%', :language, '%'))) " +
           "AND (:skill IS NULL OR :skill = '' OR LOWER(COALESCE(u.skills, '')) LIKE LOWER(CONCAT('%', :skill, '%'))) " +
           "AND (:categoryId IS NULL OR u.defaultCategoryId = :categoryId) " +
           "AND (:profileVerified IS NULL OR u.profileVerified = :profileVerified) " +
           "AND (:profileComplete IS NULL OR (u.profileCompleteness IS NOT NULL AND u.profileCompleteness >= 100))")
    Page<User> findWorkers(@Param("role") Role role, @Param("keyword") String keyword,
                           @Param("employmentType") String employmentType, @Param("language") String language,
                           @Param("skill") String skill, @Param("categoryId") Long categoryId,
                           @Param("profileVerified") Boolean profileVerified,
                           @Param("profileComplete") Boolean profileComplete,
                           Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isVerified = false AND u.role = :role")
    List<User> findUnverifiedByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(Role role);
}
