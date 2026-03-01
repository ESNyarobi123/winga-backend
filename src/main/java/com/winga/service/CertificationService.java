package com.winga.service;

import com.winga.domain.enums.ModerationStatus;
import com.winga.dto.request.CertificationRequest;
import com.winga.dto.response.CertificationResponse;
import com.winga.entity.Certification;
import com.winga.entity.User;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.CertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationRepository certificationRepository;

    @Transactional(readOnly = true)
    public List<CertificationResponse> getMyCertifications(Long userId) {
        return certificationRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> getCertificationsByUserId(Long userId) {
        return certificationRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .filter(c -> c.getModerationStatus() == ModerationStatus.APPROVED)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CertificationResponse create(User user, CertificationRequest request) {
        Certification cert = Certification.builder()
                .user(user)
                .name(request.name())
                .issuer(request.issuer())
                .fileUrl(request.fileUrl())
                .issuedAt(request.issuedAt())
                .moderationStatus(ModerationStatus.PENDING_APPROVAL)
                .build();
        return toResponse(certificationRepository.save(cert));
    }

    @Transactional
    public CertificationResponse update(Long id, User user, CertificationRequest request) {
        Certification cert = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", id));
        if (!cert.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException();
        }
        cert.setName(request.name());
        cert.setIssuer(request.issuer());
        cert.setFileUrl(request.fileUrl());
        if (request.issuedAt() != null) cert.setIssuedAt(request.issuedAt());
        return toResponse(certificationRepository.save(cert));
    }

    /** Delete certification (owner only). */
    @Transactional
    public void delete(Long id, User user) {
        Certification cert = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", id));
        if (!cert.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException();
        }
        certificationRepository.delete(cert);
    }

    @Transactional(readOnly = true)
    public Page<CertificationResponse> listByModerationStatus(ModerationStatus status, Pageable pageable) {
        return certificationRepository.findByModerationStatus(status, pageable).map(this::toResponse);
    }

    @Transactional
    public CertificationResponse setModerationStatus(Long id, ModerationStatus status) {
        Certification cert = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", id));
        cert.setModerationStatus(status);
        return toResponse(certificationRepository.save(cert));
    }

    private CertificationResponse toResponse(Certification c) {
        return new CertificationResponse(
                c.getId(), c.getName(), c.getIssuer(), c.getFileUrl(), c.getIssuedAt(),
                c.getModerationStatus(), c.getCreatedAt());
    }
}
