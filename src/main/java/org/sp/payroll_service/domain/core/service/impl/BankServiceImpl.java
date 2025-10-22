package org.sp.payroll_service.domain.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.core.dto.BankCreateRequest;
import org.sp.payroll_service.api.core.dto.BankFilter;
import org.sp.payroll_service.api.core.dto.BankResponse;
import org.sp.payroll_service.api.core.dto.BankUpdateRequest;
import org.sp.payroll_service.domain.common.exception.DuplicateEntryException;
import org.sp.payroll_service.domain.common.service.AbstractCrudService;
import org.sp.payroll_service.domain.core.entity.Bank;
import org.sp.payroll_service.domain.core.service.BankService;
import org.sp.payroll_service.repository.BankRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Concrete service implementation for managing {@code Bank} entities.
 * All public methods are synchronous (blocking).
 */
@Service
@Slf4j
public class BankServiceImpl extends AbstractCrudService<
        Bank,
        UUID,
        BankResponse,
        BankCreateRequest,
        BankUpdateRequest,
        BankFilter>
        implements BankService {

    private final BankRepository bankRepository;

    public BankServiceImpl(BankRepository bankRepository) {
        super(bankRepository, "Bank");
        this.bankRepository = bankRepository;
    }

    // --- Overrides for Creation and Update with Business Logic ---

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<BankResponse> to BankResponse
    public BankResponse create(BankCreateRequest request) {
        if (bankRepository.existsByName(request.name())) {
            throw DuplicateEntryException.forEntity("Bank", "name", request.name());
        }
        if (bankRepository.existsBySwiftBicCode(request.swiftCode())) {
            throw DuplicateEntryException.forEntity("Bank", "SWIFT Code", request.swiftCode());
        }
        return super.create(request);
    }

    @Override
    @Transactional
    // FIX: Changed return type from CompletableFuture<BankResponse> to BankResponse
    public BankResponse update(UUID id, BankUpdateRequest request) {
        checkUniquenessOnUpdate(id, request.name(), request.swiftCode());
        return super.update(id, request);
    }

    // --- Custom Search Implementation ---

    @Override
    // FIX: Removed @Async("virtualThreadExecutor")
    @Transactional(readOnly = true)
    // FIX: Changed return type from CompletableFuture<Page<BankResponse>> to Page<BankResponse>
    public Page<BankResponse> search(BankFilter filter, Pageable pageable) {
        Specification<Bank> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (StringUtils.hasText(filter.keyword())) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                Predicate keywordMatch = cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("swiftBicCode")), pattern)
                );
                predicates.add(keywordMatch);
            }

            if (StringUtils.hasText(filter.countryCode())) {
                predicates.add(cb.equal(root.get("countryCode"), filter.countryCode()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Blocking JPA call
        Page<Bank> entityPage = specExecutor.findAll(spec, pageable);
        // FIX: Return the direct Page object
        return entityPage.map(this::mapToResponse);
    }

    // --- Abstract Mapping Implementations (No changes needed) ---

    @Override
    protected Bank mapToEntity(BankCreateRequest creationRequest) {
        return Bank.builder()
                .name(creationRequest.name())
                .swiftBicCode(creationRequest.swiftCode())
                .countryCode(creationRequest.countryCode())
                .build();
    }

    @Override
    protected Bank mapToEntity(BankUpdateRequest updateRequest, Bank entity) {
        entity.setName(updateRequest.name());
        entity.setSwiftBicCode(updateRequest.swiftCode());
        entity.setCountryCode(updateRequest.countryCode());
        return entity;
    }

    @Override
    protected BankResponse mapToResponse(Bank entity) {
        return new BankResponse(
                entity.getId(),
                entity.getName(),
                entity.getSwiftBicCode(),
                entity.getCountryCode(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    // --- Private Helper: Uniqueness Check (No changes needed) ---

    private void checkUniquenessOnUpdate(UUID currentId, String newName, String newSwiftCode) {
        if (bankRepository.existsByNameAndIdNot(newName, currentId)) {
            throw DuplicateEntryException.forEntity("Bank", "name", newName);
        }
        if (bankRepository.existsBySwiftBicCodeAndIdNot(newSwiftCode, currentId)) {
            throw DuplicateEntryException.forEntity("Bank", "SWIFT Code", newSwiftCode);
        }
    }
}