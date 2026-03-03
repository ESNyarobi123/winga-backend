package com.winga.repository;

import com.winga.domain.enums.FilterOptionType;
import com.winga.entity.FilterOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterOptionRepository extends JpaRepository<FilterOption, Long> {

    List<FilterOption> findByTypeOrderBySortOrderAsc(FilterOptionType type);

    boolean existsByTypeAndSlug(FilterOptionType type, String slug);
}
