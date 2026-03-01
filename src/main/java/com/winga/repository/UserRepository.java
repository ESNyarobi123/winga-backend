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
           "OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.country) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.workType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findWorkers(@Param("role") Role role, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isVerified = false AND u.role = :role")
    List<User> findUnverifiedByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(Role role);
}
