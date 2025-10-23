package org.sp.payroll_service.domain.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.*;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Bank;
import org.sp.payroll_service.domain.core.entity.Branch;
import org.sp.payroll_service.domain.core.service.BranchService;
import org.sp.payroll_service.repository.BankRepository;
import org.sp.payroll_service.repository.BranchRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.UUID;

/**
 * Concrete service implementation for managing {@code Branch} entities.
 * All public methods are synchronous (blocking).
 */
@Service
@Slf4j
public class BranchServiceImpl extends AbstractCrudService<
        Branch,
        UUID,
        BranchResponse,
        BranchCreateRequest,
        BranchUpdateRequest,
        BranchFilter>
        implements BranchService {

    private final BranchRepository branchRepository;
    private final BankRepository bankRepository;

    public BranchServiceImpl(BranchRepository branchRepository, BankRepository bankRepository) {
        super(branchRepository, "Branch");
        this.branchRepository = branchRepository;
        this.bankRepository = bankRepository;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<BranchResponse> to BranchResponse
    public BranchResponse create(BranchCreateRequest request) {
        // Blocking uniqueness check is fine in a synchronous method
        if (branchRepository.existsByBranchNameAndBank_Id(request.branchName(), request.bankId())) {
            throw DuplicateEntryException.forEntity("Branch", "branchName", request.branchName());
        }
        return super.create(request);
    }

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<BranchResponse> to BranchResponse
    public BranchResponse update(UUID id, BranchUpdateRequest request) {
        // Blocking uniqueness check is fine
        checkUniquenessOnUpdate(id, request.branchName(), request.bankId());

        return super.update(id, request);
    }


    // --- Abstract Mapping Implementations (No changes needed) ---

    @Override
    protected Branch mapToEntity(BranchCreateRequest creationRequest) {
        // Blocking JPA call is fine in this method
        Bank bank = getBankOrThrow(creationRequest.bankId());

        return Branch.builder()
                .bank(bank)
                .branchName(creationRequest.branchName())
                .address(creationRequest.address())
                .build();
    }

    @Override
    protected Branch mapToEntity(BranchUpdateRequest updateRequest, Branch entity) {
        if (!entity.getBank().getId().equals(updateRequest.bankId())) {
            // Blocking JPA call is fine in this method
            Bank bank = getBankOrThrow(updateRequest.bankId());
            entity.setBank(bank);
        }

        entity.setBranchName(updateRequest.branchName());
        entity.setAddress(updateRequest.address());
        return entity;
    }

    @Override
    protected BranchResponse mapToResponse(Branch entity) {
        String bankName = (entity.getBank() != null) ? entity.getBank().getName() : null;

        return new BranchResponse(
                entity.getId(),
                entity.getBranchName(),
                entity.getAddress(),
                entity.getBank().getId(),
                bankName,
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    // --- Private Helpers (No changes needed) ---

    private Bank getBankOrThrow(UUID bankId) {
        return bankRepository.findById(bankId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Bank", bankId));
    }

    private void checkUniquenessOnUpdate(UUID currentId, String newBranchName, UUID bankId) {
        if (branchRepository.existsByBranchNameAndBankIdAndIdNot(newBranchName, bankId, currentId)) {
            throw DuplicateEntryException.forEntity("Branch", "branchName", newBranchName);
        }
    }

    @Override
    protected Specification<Branch> buildSpecificationFromFilter(BranchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (StringUtils.hasText(filter.keyword())) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                Predicate keywordMatch = cb.or(
                        cb.like(cb.lower(root.get("branchName")), pattern),
                        cb.like(cb.lower(root.get("address")), pattern)
                );
                predicates.add(keywordMatch);
            }

            if (filter.bankId() != null) {
                predicates.add(cb.equal(root.get("bank").get("id"), filter.bankId()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

}