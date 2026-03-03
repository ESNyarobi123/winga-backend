package com.winga.service;

import com.winga.dto.request.QualificationTestRequest;
import com.winga.dto.response.QualificationTestResponse;
import com.winga.entity.QualificationTest;
import com.winga.exception.BusinessException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.repository.QualificationTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QualificationTestService {

    private final QualificationTestRepository qualificationTestRepository;

    public List<QualificationTestResponse> listActive() {
        return qualificationTestRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QualificationTestResponse> listAll() {
        return qualificationTestRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public QualificationTestResponse getById(Long id) {
        QualificationTest t = qualificationTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QualificationTest", id));
        return toResponse(t);
    }

    public QualificationTest getEntityById(Long id) {
        return qualificationTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QualificationTest", id));
    }

    @Transactional
    public QualificationTestResponse create(QualificationTestRequest request) {
        String slug = normalizeSlug(request.slug());
        if (qualificationTestRepository.existsBySlug(slug)) {
            throw new BusinessException("A test with slug '" + slug + "' already exists.");
        }
        QualificationTest t = QualificationTest.builder()
                .name(request.name().trim())
                .slug(slug)
                .testType(request.testType().trim())
                .minScore(request.minScore() != null ? request.minScore() : 0)
                .maxScore(request.maxScore() != null ? request.maxScore() : 100)
                .maxAttempts(request.maxAttempts() != null ? request.maxAttempts() : 10)
                .isActive(request.isActive())
                .sortOrder(request.sortOrder())
                .build();
        t = qualificationTestRepository.save(t);
        return toResponse(t);
    }

    @Transactional
    public QualificationTestResponse update(Long id, QualificationTestRequest request) {
        QualificationTest t = qualificationTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QualificationTest", id));
        String slug = normalizeSlug(request.slug());
        if (qualificationTestRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException("A test with slug '" + slug + "' already exists.");
        }
        t.setName(request.name().trim());
        t.setSlug(slug);
        t.setTestType(request.testType().trim());
        t.setMinScore(request.minScore() != null ? request.minScore() : 0);
        t.setMaxScore(request.maxScore() != null ? request.maxScore() : 100);
        t.setMaxAttempts(request.maxAttempts() != null ? request.maxAttempts() : 10);
        t.setIsActive(request.isActive());
        t.setSortOrder(request.sortOrder());
        t = qualificationTestRepository.save(t);
        return toResponse(t);
    }

    @Transactional
    public void delete(Long id) {
        if (!qualificationTestRepository.existsById(id)) {
            throw new ResourceNotFoundException("QualificationTest", id);
        }
        qualificationTestRepository.deleteById(id);
    }

    private static String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim().toLowerCase().replaceAll("\\s+", "-");
    }

    private QualificationTestResponse toResponse(QualificationTest t) {
        return new QualificationTestResponse(
                t.getId(), t.getName(), t.getSlug(), t.getTestType(),
                t.getMinScore(), t.getMaxScore(), t.getMaxAttempts(),
                t.getIsActive(), t.getSortOrder(), t.getCreatedAt());
    }
}
