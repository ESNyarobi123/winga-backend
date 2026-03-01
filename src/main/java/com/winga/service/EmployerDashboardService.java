package com.winga.service;

import com.winga.domain.enums.ContractStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.dto.response.EmployerDashboardOverviewResponse;
import com.winga.entity.User;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ContractRepository;
import com.winga.repository.JobRepository;
import com.winga.repository.ProposalRepository;
import com.winga.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployerDashboardService {

    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;

    @Transactional(readOnly = true)
    public EmployerDashboardOverviewResponse getOverview(User user) {
        if (user.getRole() != Role.CLIENT && user.getRole() != Role.EMPLOYER_ADMIN && user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedAccessException("Employer dashboard is for clients only.");
        }
        Long clientId = user.getId();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long activeJobs = jobRepository.findByClientIdAndStatus(clientId, com.winga.domain.enums.JobStatus.OPEN).size();
        long applicationsToday = proposalRepository.countByJobClientIdAndCreatedAtBetween(clientId, startOfToday, endOfToday);
        long applicationsThisMonth = proposalRepository.countByJobClientIdAndCreatedAtBetween(clientId, startOfMonth, LocalDateTime.now());
        long hiresMade = contractRepository.findByClientIdAndStatus(clientId, ContractStatus.ACTIVE).size()
                + contractRepository.findByClientIdAndStatus(clientId, ContractStatus.COMPLETED).size();
        long totalProposals = proposalRepository.countByJobClientId(clientId);
        long respondedProposals = proposalRepository.countByJobClientIdAndStatusNotPending(clientId);
        double responseRatePercent = totalProposals == 0 ? 0.0 : (100.0 * respondedProposals / totalProposals);

        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> dateRows = proposalRepository.countGroupByDateSinceForClient(clientId, since);
        List<EmployerDashboardOverviewResponse.ChartPoint> applicationsOverTime = dateRows.stream()
                .map(row -> new EmployerDashboardOverviewResponse.ChartPoint(
                        row[0] != null ? row[0].toString() : "",
                        row[1] != null ? ((Number) row[1]).longValue() : 0L))
                .collect(Collectors.toList());

        List<Object[]> categoryRows = proposalRepository.countByCategorySinceForClient(clientId, since);
        List<EmployerDashboardOverviewResponse.TopCategoryDto> topCategories = categoryRows.stream()
                .map(row -> new EmployerDashboardOverviewResponse.TopCategoryDto(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() : 0L))
                .collect(Collectors.toList());

        return new EmployerDashboardOverviewResponse(
                activeJobs,
                applicationsToday,
                applicationsThisMonth,
                hiresMade,
                responseRatePercent,
                applicationsOverTime,
                topCategories);
    }
}
