package com.winga.repository;

import com.winga.entity.PortfolioItem;
import com.winga.domain.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    List<PortfolioItem> findByUserIdOrderBySortOrderAsc(Long userId);

    Page<PortfolioItem> findByModerationStatus(ModerationStatus status, Pageable pageable);
}
