package com.winga.repository;

import com.winga.entity.ContractAddOn;
import com.winga.domain.enums.AddOnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractAddOnRepository extends JpaRepository<ContractAddOn, Long> {

    List<ContractAddOn> findByContractIdOrderByCreatedAtDesc(Long contractId);

    List<ContractAddOn> findByContractIdAndStatus(Long contractId, AddOnStatus status);
}
