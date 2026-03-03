package com.winga.service;

import com.winga.domain.enums.Role;
import com.winga.entity.SavedWorker;
import com.winga.entity.User;
import com.winga.dto.response.UserResponse;
import com.winga.exception.BusinessException;
import com.winga.repository.SavedWorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedWorkerService {

    private final SavedWorkerRepository savedWorkerRepository;
    private final UserService userService;

    @Transactional
    public void saveWorker(Long workerId, User user) {
        User worker = userService.getById(workerId);
        if (worker.getRole() != Role.FREELANCER) {
            throw new BusinessException("Can only save freelancers/workers.");
        }
        if (worker.getId().equals(user.getId())) {
            throw new BusinessException("Cannot save yourself.");
        }
        if (savedWorkerRepository.existsByUserIdAndWorkerId(user.getId(), workerId)) {
            throw new BusinessException("Worker already saved.");
        }
        SavedWorker saved = SavedWorker.builder()
                .user(user)
                .worker(worker)
                .build();
        savedWorkerRepository.save(saved);
        log.debug("User {} saved worker {}", user.getId(), workerId);
    }

    @Transactional
    public void unsaveWorker(Long workerId, User user) {
        savedWorkerRepository.deleteByUserIdAndWorkerId(user.getId(), workerId);
        log.debug("User {} unsaved worker {}", user.getId(), workerId);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getMySavedWorkers(Long userId, Pageable pageable) {
        return savedWorkerRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(sw -> userService.toUserResponse(sw.getWorker()));
    }

    @Transactional(readOnly = true)
    public boolean isSavedByUser(Long userId, Long workerId) {
        return savedWorkerRepository.existsByUserIdAndWorkerId(userId, workerId);
    }
}
