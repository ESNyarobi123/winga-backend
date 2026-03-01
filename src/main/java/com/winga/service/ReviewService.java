package com.winga.service;

import com.winga.domain.enums.ContractStatus;
import com.winga.dto.request.ReviewRequest;
import com.winga.dto.response.ReviewResponse;
import com.winga.entity.Contract;
import com.winga.entity.Review;
import com.winga.entity.User;
import com.winga.exception.BusinessException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContractService contractService;
    private final UserService userService;

    @Transactional
    public ReviewResponse createReview(Long contractId, User reviewer, ReviewRequest request) {
        Contract contract = contractService.getContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BusinessException("Can only review after contract is completed.");
        }
        boolean isClient = contract.getClient().getId().equals(reviewer.getId());
        boolean isFreelancer = contract.getFreelancer().getId().equals(reviewer.getId());
        if (!isClient && !isFreelancer) {
            throw new UnauthorizedAccessException("Only client or freelancer of this contract can submit a review.");
        }
        if (reviewRepository.existsByContractIdAndReviewerId(contractId, reviewer.getId())) {
            throw new BusinessException("You have already reviewed this contract.");
        }
        User reviewee = isClient ? contract.getFreelancer() : contract.getClient();
        Review review = Review.builder()
                .contract(contract)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(request.rating())
                .comment(request.comment() != null && !request.comment().isBlank() ? request.comment() : null)
                .build();
        review = reviewRepository.save(review);
        log.info("Review created: contract={} reviewer={} reviewee={} rating={}", contractId, reviewer.getId(), reviewee.getId(), request.rating());
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForUser(Long revieweeId, Pageable pageable) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(revieweeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long userId) {
        return reviewRepository.getAverageRatingByRevieweeId(userId);
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Long userId) {
        return reviewRepository.countByRevieweeId(userId);
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getContract().getId(),
                userService.toUserResponse(r.getReviewer()),
                userService.toUserResponse(r.getReviewee()),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
