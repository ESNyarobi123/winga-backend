package com.winga.service;

import com.winga.domain.enums.ContractStatus;
import com.winga.domain.enums.ProposalStatus;
import com.winga.dto.response.FreelancerDashboardResponse;
import com.winga.dto.response.WalletResponse;
import com.winga.repository.ContractRepository;
import com.winga.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FreelancerService {

    private static final List<ContractStatus> ACTIVE_CONTRACT_STATUSES = List.of(
            ContractStatus.ACTIVE,
            ContractStatus.PAUSED,
            ContractStatus.REVIEW_PENDING,
            ContractStatus.DISPUTED
    );

    private final ContractRepository contractRepository;
    private final ProposalRepository proposalRepository;
    private final WalletService walletService;

    /**
     * Dashboard for worker: balance, total earned, active contracts count, pending proposals count.
     */
    @Transactional(readOnly = true)
    public FreelancerDashboardResponse getDashboard(Long freelancerId) {
        WalletResponse wallet = walletService.getBalance(freelancerId);
        long activeContracts = contractRepository.countByFreelancerIdAndStatusIn(
                freelancerId, ACTIVE_CONTRACT_STATUSES);
        long pendingProposals = proposalRepository.countByFreelancerIdAndStatus(
                freelancerId, ProposalStatus.PENDING);

        return new FreelancerDashboardResponse(
                wallet.balance(),
                wallet.totalEarned(),
                wallet.currency(),
                activeContracts,
                pendingProposals
        );
    }
}
