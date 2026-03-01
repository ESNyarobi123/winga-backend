package com.winga.repository;

import com.winga.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId, Pageable pageable);

    Optional<Review> findByContractIdAndReviewerId(Long contractId, Long reviewerId);

    boolean existsByContractIdAndReviewerId(Long contractId, Long reviewerId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.reviewee.id = :userId")
    double getAverageRatingByRevieweeId(Long userId);

    long countByRevieweeId(Long revieweeId);
}
