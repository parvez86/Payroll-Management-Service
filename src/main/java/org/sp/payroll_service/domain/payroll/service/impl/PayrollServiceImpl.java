package org.sp.payroll_service.domain.payroll.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sp.payroll_service.api.payroll.dto.*;
import org.sp.payroll_service.api.payroll.mapper.PayrollBatchMapper;
import org.sp.payroll_service.api.payroll.mapper.PayrollItemMapper;
import org.sp.payroll_service.domain.common.dto.response.Money;
import org.sp.payroll_service.domain.common.enums.PayrollItemStatus;
import org.sp.payroll_service.domain.common.enums.PayrollStatus;
import org.sp.payroll_service.domain.common.exception.ResourceNotFoundException;
import org.sp.payroll_service.domain.core.entity.Company;
import org.sp.payroll_service.domain.payroll.entity.Employee;
import org.sp.payroll_service.domain.payroll.entity.PayrollBatch;
import org.sp.payroll_service.domain.payroll.entity.PayrollItem;
import org.sp.payroll_service.domain.payroll.entity.SalaryDistributionFormula;
import org.sp.payroll_service.domain.payroll.exception.InsufficientFundsException;
import org.sp.payroll_service.domain.payroll.exception.PayrollProcessingException;
import org.sp.payroll_service.domain.payroll.service.PayrollService;
import org.sp.payroll_service.domain.payroll.service.SalaryCalculationService;
import org.sp.payroll_service.domain.payroll.service.TransactionService;
import org.sp.payroll_service.domain.payroll.service.transaction.TransactionStrategyService;
import org.sp.payroll_service.domain.wallet.entity.Account;
import org.sp.payroll_service.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for payroll processing operations.
 * Provides ACID-compliant payroll processing with financial integrity.
 * <p>
 * NOTE: All methods are now synchronous (blocking). Asynchronous execution using
 * Virtual Threads is handled by the calling Controller layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final PayrollBatchRepository payrollBatchRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;
    private final SalaryDistributionFormulaRepository salaryDistributionFormulaRepository;

    private final SalaryCalculationService salaryCalculationService;
    private final TransactionService transactionService;
    private final TransactionStrategyService transactionStrategyService;
    private final PayrollBatchMapper payrollBatchMapper;
    private final PayrollItemMapper payrollItemMapper;

    @Override
    @Transactional
    public PayrollBatchResponse createPayrollBatch(CreatePayrollBatchRequest request) {
        log.info("Creating payroll batch: {} for company: {}", request.name(), request.companyId());

        // Validate company exists
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", request.companyId()));

        // Validate funding account exists
        Account fundingAccount = accountRepository.findById(request.fundingAccountId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", request.fundingAccountId()));

        // Create payroll batch
        PayrollBatch batch = PayrollBatch.builder()
                .name(request.name())
                .payrollMonth(request.payrollMonth())
                .company(company)
                .fundingAccountId(request.fundingAccountId())
                .description(request.description())
                .build();

        PayrollBatch savedBatch = payrollBatchRepository.save(batch);

        // Generate payroll items for all employees
        generatePayrollItems(savedBatch);

        // Calculate statistics
        Integer employeeCount = getEmployeeCountForBatch(savedBatch.getId());
        Money totalAmount = Money.of(payrollItemRepository.getTotalAmountForBatch(savedBatch.getId()));

        log.info("Created payroll batch: {} with {} employees, total amount: {}",
                savedBatch.getId(), employeeCount, totalAmount);

        return payrollBatchMapper.toResponse(savedBatch, employeeCount, 0, 0, totalAmount, Money.of(BigDecimal.ZERO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryCalculation> calculateSalaries(UUID batchId) {
        log.info("Calculating salaries for batch: {}", batchId);

        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", batchId));

        List<PayrollItem> payrollItems = payrollItemRepository.findAllByPayrollBatchId(batchId);

        return payrollItems.stream()
                .map(item -> payrollItemMapper.toSalaryCalculation(item.getEmployee(), item))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryCalculation> calculateSalariesForCompany(UUID companyId) {
        log.info("Calculating salaries for company: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Company", companyId));

        List<Employee> employees = employeeRepository.findAllOrderedByGrade();
        SalaryDistributionFormula formula = company.getSalaryFormula();

        return employees.stream()
                .map(employee -> {
                    PayrollItem item = salaryCalculationService.calculateSalary(employee, formula);
                    return payrollItemMapper.toSalaryCalculation(employee, item);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PayrollResult processPayroll(UUID batchId) {
        log.warn("Processing payroll batch with ACID transactions: {}", batchId);

        try {
            PayrollBatch batch = payrollBatchRepository.findById(batchId)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", batchId));

            // Validate batch transactionStatus
            if (batch.getPayrollStatus() != PayrollStatus.PENDING) {
                throw new PayrollProcessingException("Batch is not in PENDING status: " + batch.getPayrollStatus());
            }

            // Get company account
            Account companyAccount = accountRepository.findById(batch.getFundingAccountId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Account", batch.getFundingAccountId()));

            // Get all payroll items for this batch
            List<PayrollItem> payrollItems = payrollItemRepository.findAllByPayrollBatchId(batchId);

            // Calculate total payroll amount
            BigDecimal totalAmount = payrollItems.stream()
                    .map(PayrollItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Money companyBalanceBefore = Money.of(companyAccount.getCurrentBalance());

            // Check sufficient funds
            if (companyAccount.getCurrentBalance().compareTo(totalAmount) < 0) {
                batch.setPayrollStatus(PayrollStatus.FAILED);
                payrollBatchRepository.save(batch);
                throw new InsufficientFundsException(
                        String.format("Insufficient funds. Required: %s, Available: %s",
                                totalAmount, companyAccount.getCurrentBalance()));
            }

            // Mark batch as processing
            batch.setPayrollStatus(PayrollStatus.PROCESSING);
            payrollBatchRepository.save(batch);

            // Process each payroll item
            List<PayrollItemResponse> processedItems = new ArrayList<>();
            BigDecimal processedAmount = BigDecimal.ZERO;
            BigDecimal failedAmount = BigDecimal.ZERO;
            int successfulPayments = 0;
            int failedPayments = 0;
            List<String> errorMessages = new ArrayList<>();

            for (PayrollItem item : payrollItems) {
                try {
                    item.setPayrollItemStatus(PayrollItemStatus.PROCESSING);
                    payrollItemRepository.save(item);

                    // Execute transfer (assuming TransactionService is now synchronous)
                    TransferRequest transferRequest = TransferRequest.builder()
                            .debitAccountId(companyAccount.getId())
                            .creditAccountId(item.getEmployee().getAccount().getId())
                            .amount(item.getAmount())
                            .referenceId("PAYROLL-" + batchId + "-" + item.getEmployee().getCode())
                            .description("Salary payment for " + item.getEmployee().getName())
                            .build();

                    // FIX: Assuming executeTransfer is now synchronous
                    transactionService.executeTransfer(transferRequest);

                    item.setPayrollItemStatus(PayrollItemStatus.PAID);
                    item.setExecutedAt(Instant.now());
                    processedAmount = processedAmount.add(item.getAmount());
                    successfulPayments++;

                    log.debug("Successfully paid employee: {} amount: {}",
                            item.getEmployee().getCode(), item.getAmount());

                } catch (Exception e) {
                    item.setPayrollItemStatus(PayrollItemStatus.FAILED);
                    item.setFailureReason(e.getMessage());
                    item.setExecutedAt(Instant.now());
                    failedAmount = failedAmount.add(item.getAmount());
                    failedPayments++;
                    errorMessages.add("Employee " + item.getEmployee().getCode() + ": " + e.getMessage());

                    log.error("Failed to pay employee: {} - {}", item.getEmployee().getCode(), e.getMessage());
                }

                payrollItemRepository.save(item);
                processedItems.add(payrollItemMapper.toResponse(item));
            }

            // Update batch status
            if (failedPayments == 0) {
                batch.setPayrollStatus(PayrollStatus.COMPLETED);
            } else if (successfulPayments > 0) {
                batch.setPayrollStatus(PayrollStatus.PARTIALLY_COMPLETED);
            } else {
                batch.setPayrollStatus(PayrollStatus.FAILED);
            }

            batch.setTotalAmount(totalAmount);
            batch.setExecutedAmount(processedAmount);
            batch.setExecutedAt(Instant.now());
            payrollBatchRepository.save(batch);

            // Get updated company balance
            Account updatedCompanyAccount = accountRepository.findById(companyAccount.getId()).get();
            Money companyBalanceAfter = Money.of(updatedCompanyAccount.getCurrentBalance());

            String message = String.format("Payroll processing completed. Success: %d, Failed: %d",
                    successfulPayments, failedPayments);

            log.info("Payroll batch {} processing completed: {}", batchId, message);

            return PayrollResult.builder()
                    .success(failedPayments == 0)
                    .batchId(batchId)
                    .batchStatus(batch.getPayrollStatus())
                    .totalAmount(Money.of(totalAmount))
                    .processedAmount(Money.of(processedAmount))
                    .failedAmount(Money.of(failedAmount))
                    .totalEmployees(payrollItems.size())
                    .successfulPayments(successfulPayments)
                    .failedPayments(failedPayments)
                    .companyBalanceBefore(companyBalanceBefore)
                    .companyBalanceAfter(companyBalanceAfter)
                    .payrollItems(processedItems)
                    .errorMessages(errorMessages)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("Payroll processing failed for batch: {}", batchId, e);
            throw new PayrollProcessingException("Payroll processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollBatchSummary> getAllBatches(PayrollBatchFilter filter, Pageable pageable) {
        log.debug("Retrieving payroll batches with filter: {}", filter);

        Specification<PayrollBatch> spec = createSpecification(filter);
        Page<PayrollBatch> batchPage = payrollBatchRepository.findAll(spec, pageable);

        return batchPage.map(batch -> {
            Integer employeeCount = getEmployeeCountForBatch(batch.getId());
            Integer successfulPayments = getSuccessfulPaymentCount(batch.getId());
            Integer failedPayments = getFailedPaymentCount(batch.getId());
            Money totalAmount = Money.of(payrollItemRepository.getTotalAmountForBatch(batch.getId()));

            return payrollBatchMapper.toSummary(batch, employeeCount, successfulPayments, failedPayments, totalAmount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollBatchResponse getBatchById(UUID batchId) {
        log.debug("Retrieving payroll batch: {}", batchId);

        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", batchId));

        Integer employeeCount = getEmployeeCountForBatch(batchId);
        Integer successfulPayments = getSuccessfulPaymentCount(batchId);
        Integer failedPayments = getFailedPaymentCount(batchId);
        Money totalAmount = Money.of(payrollItemRepository.getTotalAmountForBatch(batchId));
        Money executedAmount = Money.of(payrollItemRepository.getTotalPaidAmountForBatch(batchId));

        return payrollBatchMapper.toResponse(batch, employeeCount, successfulPayments, failedPayments, totalAmount, executedAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollItemResponse> getBatchItems(UUID batchId, Pageable pageable) {
        log.debug("Retrieving payroll items for batch: {}", batchId);

        // Verify batch exists
        payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", batchId));

        Page<PayrollItem> itemPage = payrollItemRepository.findByPayrollBatchId(batchId, pageable);
        return itemPage.map(payrollItemMapper::toResponse);
    }

    @Override
    @Transactional
    public PayrollItemResponse retryPayrollItem(UUID payrollItemId) {
        log.info("Retrying payroll item: {}", payrollItemId);

        PayrollItem item = payrollItemRepository.findById(payrollItemId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollItem", payrollItemId));

        if (item.getPayrollItemStatus() != PayrollItemStatus.FAILED) {
            throw new PayrollProcessingException("Payroll item is not in FAILED status: " + item.getPayrollItemStatus());
        }

        // Reset item for retry
        item.setPayrollItemStatus(PayrollItemStatus.PROCESSING);
        item.setFailureReason(null);
        item.setExecutedAt(null);

        PayrollItem savedItem = payrollItemRepository.save(item);
        return payrollItemMapper.toResponse(savedItem);
    }

    @Override
    @Transactional
    public PayrollBatchResponse cancelBatch(UUID batchId) {
        log.info("Cancelling payroll batch: {}", batchId);

        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PayrollBatch", batchId));

        if (batch.getPayrollStatus() != PayrollStatus.PENDING) {
            throw new PayrollProcessingException("Cannot cancel batch in status: " + batch.getPayrollStatus());
        }

        batch.setPayrollStatus(PayrollStatus.CANCELLED);
        PayrollBatch savedBatch = payrollBatchRepository.save(batch);

        Integer employeeCount = getEmployeeCountForBatch(batchId);
        Money totalAmount = Money.of(payrollItemRepository.getTotalAmountForBatch(batchId));

        return payrollBatchMapper.toResponse(savedBatch, employeeCount, 0, 0, totalAmount, Money.of(BigDecimal.ZERO));
    }

    // --- Helper Methods ---

    private void generatePayrollItems(PayrollBatch batch) {
        List<Employee> employees = employeeRepository.findAllOrderedByGrade();
        SalaryDistributionFormula formula = batch.getCompany().getSalaryFormula();

        for (Employee employee : employees) {
            PayrollItem item = salaryCalculationService.calculateSalary(employee, formula);
            item.setPayrollBatch(batch);
            payrollItemRepository.save(item);
        }

        log.info("Generated {} payroll items for batch {}", employees.size(), batch.getId());
    }

    private Specification<PayrollBatch> createSpecification(PayrollBatchFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("payrollStatus"), filter.status()));
            }

            if (filter.companyId() != null) {
                predicates.add(cb.equal(root.get("company").get("id"), filter.companyId()));
            }

            if (filter.fromMonth() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("payrollMonth"), filter.fromMonth()));
            }

            if (filter.toMonth() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("payrollMonth"), filter.toMonth()));
            }

            if (filter.nameContains() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.nameContains().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Integer getEmployeeCountForBatch(UUID batchId) {
        return Math.toIntExact(payrollItemRepository.countByPayrollBatchId(batchId));
    }

    private Integer getSuccessfulPaymentCount(UUID batchId) {
        return Math.toIntExact(payrollItemRepository.countByBatchIdAndStatus(batchId, PayrollItemStatus.PAID));
    }

    private Integer getFailedPaymentCount(UUID batchId) {
        return Math.toIntExact(payrollItemRepository.countByBatchIdAndStatus(batchId, PayrollItemStatus.FAILED));
    }
}
