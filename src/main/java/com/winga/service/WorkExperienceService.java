package com.winga.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winga.dto.request.WorkExperienceRequest;
import com.winga.dto.response.WorkExperienceResponse;
import com.winga.entity.User;
import com.winga.entity.WorkExperience;
import com.winga.exception.ResourceNotFoundException;
import com.winga.repository.WorkExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;
    private final ObjectMapper objectMapper;

    public List<WorkExperienceResponse> getMyExperiences(Long userId) {
        return workExperienceRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkExperienceResponse addExperience(User user, WorkExperienceRequest request) {
        String skillsLearnedJson = serializeSkillsLearned(request.skillsLearned());
        WorkExperience exp = WorkExperience.builder()
                .user(user)
                .title(request.title().trim())
                .company(request.company() != null ? request.company().trim() : null)
                .startDate(request.startDate() != null ? request.startDate().trim() : null)
                .endDate(request.endDate() != null ? request.endDate().trim() : null)
                .description(request.description() != null ? request.description().trim() : null)
                .skillsLearned(skillsLearnedJson)
                .build();
        exp = workExperienceRepository.save(exp);
        return toResponse(exp);
    }

    @Transactional
    public void deleteExperience(Long experienceId, Long userId) {
        if (!workExperienceRepository.existsByIdAndUserId(experienceId, userId)) {
            throw new ResourceNotFoundException("Work experience", experienceId);
        }
        workExperienceRepository.deleteById(experienceId);
    }

    @Transactional
    public List<WorkExperienceResponse> replaceMyExperiences(User user, List<WorkExperienceRequest> requests) {
        workExperienceRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .forEach(workExperienceRepository::delete);
        return requests.stream()
                .map(req -> addExperience(user, req))
                .collect(Collectors.toList());
    }

    private WorkExperienceResponse toResponse(WorkExperience e) {
        return new WorkExperienceResponse(
                e.getId(),
                e.getTitle(),
                e.getCompany(),
                e.getStartDate(),
                e.getEndDate(),
                e.getDescription(),
                parseSkillsLearned(e.getSkillsLearned()),
                e.getCreatedAt()
        );
    }

    private String serializeSkillsLearned(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> parseSkillsLearned(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
