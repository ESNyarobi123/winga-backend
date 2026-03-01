package com.winga.repository;

import com.winga.entity.Certification;
import com.winga.domain.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findByUserIdOrderByIssuedAtDesc(Long userId);

    Page<Certification> findByModerationStatus(ModerationStatus status, Pageable pageable);
}
