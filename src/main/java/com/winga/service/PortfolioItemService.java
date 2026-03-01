package com.winga.service;

import com.winga.domain.enums.ModerationStatus;
import com.winga.dto.request.PortfolioItemRequest;
import com.winga.dto.response.PortfolioItemResponse;
import com.winga.entity.PortfolioItem;
import com.winga.entity.User;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.PortfolioItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioItemService {

    private final PortfolioItemRepository portfolioItemRepository;

    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> getMyPortfolio(Long userId) {
        return portfolioItemRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> getPortfolioByUserId(Long userId) {
        return portfolioItemRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .filter(p -> p.getModerationStatus() == ModerationStatus.APPROVED)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PortfolioItemResponse create(User user, PortfolioItemRequest request) {
        PortfolioItem item = PortfolioItem.builder()
                .user(user)
                .type(request.type().toUpperCase())
                .url(request.url())
                .title(request.title())
                .description(request.description())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .moderationStatus(ModerationStatus.PENDING_APPROVAL)
                .build();
        return toResponse(portfolioItemRepository.save(item));
    }

    @Transactional
    public PortfolioItemResponse update(Long id, User user, PortfolioItemRequest request) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item", id));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException();
        }
        item.setType(request.type().toUpperCase());
        item.setUrl(request.url());
        item.setTitle(request.title());
        item.setDescription(request.description());
        if (request.sortOrder() != null) item.setSortOrder(request.sortOrder());
        return toResponse(portfolioItemRepository.save(item));
    }

    @Transactional
    public void delete(Long id, User user) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item", id));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException();
        }
        portfolioItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public Page<PortfolioItemResponse> listByModerationStatus(ModerationStatus status, Pageable pageable) {
        return portfolioItemRepository.findByModerationStatus(status, pageable).map(this::toResponse);
    }

    @Transactional
    public PortfolioItemResponse setModerationStatus(Long id, ModerationStatus status) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item", id));
        item.setModerationStatus(status);
        return toResponse(portfolioItemRepository.save(item));
    }

    private PortfolioItemResponse toResponse(PortfolioItem p) {
        return new PortfolioItemResponse(
                p.getId(), p.getType(), p.getUrl(), p.getTitle(), p.getDescription(),
                p.getSortOrder(), p.getModerationStatus(), p.getCreatedAt());
    }
}
