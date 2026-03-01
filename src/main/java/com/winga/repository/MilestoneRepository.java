package com.winga.repository;

import com.winga.entity.Milestone;
import com.winga.domain.enums.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByContractIdOrderByOrderIndex(Long contractId);

    List<Milestone> findByContractIdAndStatus(Long contractId, MilestoneStatus status);

    long countByContractIdAndStatus(Long contractId, MilestoneStatus status);
}
