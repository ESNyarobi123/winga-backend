package com.winga.service;

import com.winga.domain.enums.AddOnStatus;
import com.winga.domain.enums.ContractStatus;
import com.winga.dto.request.AddOnRequest;
import com.winga.dto.response.AddOnResponse;
import com.winga.entity.Contract;
import com.winga.entity.ContractAddOn;
import com.winga.entity.User;
import com.winga.exception.BusinessException;
import com.winga.exception.InsufficientFundsException;
import com.winga.exception.ResourceNotFoundException;
import com.winga.exception.UnauthorizedAccessException;
import com.winga.repository.ContractAddOnRepository;
import com.winga.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractAddOnService {

    private final ContractAddOnRepository addOnRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final WalletService walletService;
    private final NotificationService notificationService;

    @Value("${app.platform.commission-rate:0.15}")
    private BigDecimal commissionRate;

    @Transactional
    public AddOnResponse propose(Long contractId, User freelancer, AddOnRequest request) {
        Contract contract = contractService.getContractOrThrow(contractId);
        if (!contract.getFreelancer().getId().equals(freelancer.getId())) {
            throw new UnauthorizedAccessException("Only the freelancer on this contract can propose add-ons.");
        }
        if (contract.getStatus() == ContractStatus.DISPUTED ||
                contract.getStatus() == ContractStatus.TERMINATED ||
                contract.getStatus() == ContractStatus.COMPLETED) {
            throw new BusinessException("Cannot propose add-on on a closed or disputed contract.");
        }
        ContractAddOn addOn = ContractAddOn.builder()
                .contract(contract)
                .title(request.title())
                .description(request.description())
                .amount(request.amount())
                .status(AddOnStatus.PROPOSED)
                .build();
        addOn = addOnRepository.save(addOn);
        notificationService.notify(contract.getClient(),
                com.winga.domain.enums.NotificationType.PROPOSAL_RECEIVED,
                "Add-on inapendekezwa",
                "Freelancer amependekeza: " + request.title() + " — TZS " + request.amount() + ". Accept & Deposit kwenye contract.",
                String.valueOf(contractId), "CONTRACT");
        log.info("Add-on proposed: contract={} title={} amount={}", contractId, request.title(), request.amount());
        return toResponse(addOn);
    }

    @Transactional
    public AddOnResponse accept(Long addOnId, User client) {
        ContractAddOn addOn = addOnRepository.findById(addOnId)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on", addOnId));
        Contract contract = addOn.getContract();
        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can accept add-ons.");
        }
        if (addOn.getStatus() != AddOnStatus.PROPOSED) {
            throw new BusinessException("This add-on is not pending.");
        }
        if (!walletService.hasSufficientFunds(client.getId(), addOn.getAmount())) {
            throw new InsufficientFundsException(
                    "Balance haitoshi. Unahitaji TZS " + addOn.getAmount() + " kwa add-on: " + addOn.getTitle());
        }
        walletService.debitWallet(client.getId(), addOn.getAmount(),
                "Add-on: " + addOn.getTitle(),
                "A-" + addOnId);
        contract.setAddonEscrowAmount(contract.getAddonEscrowAmount().add(addOn.getAmount()));
        contractRepository.save(contract);
        addOn.setStatus(AddOnStatus.ACCEPTED);
        addOn.setAcceptedAt(LocalDateTime.now());
        addOnRepository.save(addOn);
        notificationService.notify(contract.getFreelancer(),
                com.winga.domain.enums.NotificationType.HIRED,
                "Add-on imekubaliwa",
                "Mteja amekubali na kulipia: " + addOn.getTitle() + ". Unaweza kuanza kazi.",
                String.valueOf(contract.getId()), "CONTRACT");
        log.info("Add-on accepted: addOnId={} contract={}", addOnId, contract.getId());
        return toResponse(addOn);
    }

    @Transactional
    public AddOnResponse reject(Long addOnId, User client) {
        ContractAddOn addOn = addOnRepository.findById(addOnId)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on", addOnId));
        if (!addOn.getContract().getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can reject add-ons.");
        }
        if (addOn.getStatus() != AddOnStatus.PROPOSED) {
            throw new BusinessException("This add-on is not pending.");
        }
        addOn.setStatus(AddOnStatus.REJECTED);
        addOn = addOnRepository.save(addOn);
        return toResponse(addOn);
    }

    @Transactional
    public AddOnResponse complete(Long addOnId, User client) {
        ContractAddOn addOn = addOnRepository.findById(addOnId)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on", addOnId));
        Contract contract = addOn.getContract();
        if (!contract.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedAccessException("Only the client can mark add-on as complete.");
        }
        if (addOn.getStatus() != AddOnStatus.ACCEPTED) {
            throw new BusinessException("Add-on must be accepted first.");
        }
        BigDecimal amount = addOn.getAmount();
        BigDecimal fee = amount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal freelancerPay = amount.subtract(fee);
        walletService.creditWallet(contract.getFreelancer().getId(), freelancerPay,
                "Add-on completed: " + addOn.getTitle(),
                "A-" + addOnId);
        contract.setAddonEscrowAmount(contract.getAddonEscrowAmount().subtract(amount));
        contract.setPlatformFeeCollected(contract.getPlatformFeeCollected().add(fee));
        contractRepository.save(contract);
        addOn.setStatus(AddOnStatus.COMPLETED);
        addOn.setCompletedAt(LocalDateTime.now());
        addOnRepository.save(addOn);
        notificationService.notifyPaymentReleased(contract.getFreelancer(), freelancerPay.toPlainString(), contract.getId());
        log.info("Add-on completed: addOnId={} released={}", addOnId, freelancerPay);
        return toResponse(addOn);
    }

    @Transactional(readOnly = true)
    public List<AddOnResponse> listByContract(Long contractId, User user) {
        Contract contract = contractService.getContractOrThrow(contractId);
        if (!contract.getClient().getId().equals(user.getId()) && !contract.getFreelancer().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Not a party to this contract.");
        }
        return addOnRepository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AddOnResponse toResponse(ContractAddOn a) {
        return new AddOnResponse(
                a.getId(),
                a.getContract().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getAmount(),
                a.getStatus(),
                a.getAcceptedAt(),
                a.getCompletedAt(),
                a.getCreatedAt()
        );
    }
}
