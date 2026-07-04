package com.example.loanengine.service;

import com.example.loanengine.domain.Loan;
import com.example.loanengine.domain.ScheduleEntry;
import com.example.loanengine.domain.LoanTransaction;
import com.example.loanengine.dto.LoanRequest;
import com.example.loanengine.dto.PrepaymentRequest;
import com.example.loanengine.dto.SettlementRequest;
import com.example.loanengine.dto.SettlementResponse;
import com.example.loanengine.repository.LoanRepository;
import com.example.loanengine.repository.LoanTransactionRepository;
import com.example.loanengine.repository.ScheduleEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final LoanTransactionRepository transactionRepository;

    public LoanService(LoanRepository loanRepository, ScheduleEntryRepository scheduleEntryRepository, LoanTransactionRepository transactionRepository) {
        this.loanRepository = loanRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
        this.transactionRepository = transactionRepository;
    }

    private static final int MONEY_SCALE = 2;

    public Loan createLoan(LoanRequest request) {
        validateLoanRequest(request);

        BigDecimal monthlyRate = request.getAnnualInterestRate().divide(BigDecimal.valueOf(12), MathContext.DECIMAL128);
        BigDecimal onePlusRatePower = BigDecimal.ONE.add(monthlyRate).pow(request.getTenorMonths(), MathContext.DECIMAL128);
        BigDecimal emi = request.getPrincipal().multiply(monthlyRate).multiply(onePlusRatePower)
                .divide(onePlusRatePower.subtract(BigDecimal.ONE), 8, RoundingMode.HALF_UP)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        Loan loan = new Loan(
                Optional.ofNullable(request.getProductName()).orElse("Standard Loan"),
                request.getPrincipal().setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                request.getAnnualInterestRate().setScale(6, RoundingMode.HALF_UP),
                request.getTenorMonths(),
                emi,
                request.getStartDate(),
                "ACTIVE"
        );

        loan = loanRepository.save(loan);
        List<ScheduleEntry> schedule = generateSchedule(loan);
        scheduleEntryRepository.saveAll(schedule);
        loan.getScheduleEntries().addAll(schedule);
        return loan;
    }

    private void validateLoanRequest(LoanRequest request) {
        if (request.getPrincipal() == null || request.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be greater than zero.");
        }
        if (request.getAnnualInterestRate() == null || request.getAnnualInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Annual interest rate must be greater than zero.");
        }
        if (request.getTenorMonths() == null || request.getTenorMonths() <= 0) {
            throw new IllegalArgumentException("Tenor months must be greater than zero.");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
    }

    private List<ScheduleEntry> generateSchedule(Loan loan) {
        BigDecimal balance = loan.getPrincipal();
        BigDecimal monthlyRate = loan.getAnnualInterestRate().divide(BigDecimal.valueOf(12), MathContext.DECIMAL128);
        LocalDate dueDate = loan.getStartDate();

        List<ScheduleEntry> schedule = new java.util.ArrayList<>();
        for (int i = 1; i <= loan.getTenorMonths(); i++) {
            BigDecimal interest = balance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent = loan.getMonthlyEmi().subtract(interest).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal closingBalance = balance.subtract(principalComponent).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (i == loan.getTenorMonths()) {
                principalComponent = balance;
                interest = loan.getMonthlyEmi().subtract(principalComponent).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                closingBalance = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            }
            ScheduleEntry entry = new ScheduleEntry(
                    loan,
                    i,
                    dueDate.plusMonths(i - 1),
                    balance.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                    loan.getMonthlyEmi(),
                    principalComponent,
                    interest,
                    closingBalance,
                    "PENDING"
            );
            schedule.add(entry);
            balance = closingBalance;
        }
        return schedule;
    }

    public List<ScheduleEntry> getSchedule(Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new EntityNotFoundException("Loan not found");
        }
        return scheduleEntryRepository.findByLoanIdOrderByInstallmentNumber(loanId);
    }

    @Transactional
    public void processPrepayment(Long loanId, PrepaymentRequest request) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new EntityNotFoundException("Loan not found"));
        validatePrepaymentRequest(request);

        List<ScheduleEntry> schedule = scheduleEntryRepository.findByLoanIdOrderByInstallmentNumber(loanId);
        int targetIndex = request.getInstallmentNumber() - 1;
        if (targetIndex < 0 || targetIndex >= schedule.size()) {
            throw new IllegalArgumentException("Invalid installment number.");
        }
        ScheduleEntry targetEntry = schedule.get(targetIndex);
        BigDecimal outstandingPrincipal = targetEntry.getOpeningBalance();

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prepayment amount must be greater than zero.");
        }
        if (request.getAmount().compareTo(outstandingPrincipal) > 0) {
            throw new IllegalArgumentException("Prepayment cannot exceed outstanding principal.");
        }

        String option = request.getOption();
        switch (option) {
            case "A" -> applyReduceEmiKeepTenor(loan, schedule, targetIndex, request.getAmount());
            case "B" -> applyKeepEmiReduceTenor(loan, schedule, targetIndex, request.getAmount());
            case "C" -> applyAdvanceInstallments(loan, schedule, targetIndex, request.getAmount());
            default -> throw new IllegalArgumentException("Unsupported prepayment option.");
        }

        loanRepository.save(loan);
        transactionRepository.save(new LoanTransaction(loan, "PREPAYMENT", option, request.getAmount(), request.getInstallmentNumber(), LocalDateTime.now(), "Processed prepayment option " + option));
    }

    private void validatePrepaymentRequest(PrepaymentRequest request) {
        if (request.getInstallmentNumber() == null || request.getInstallmentNumber() <= 0) {
            throw new IllegalArgumentException("Installment number is required and must be positive.");
        }
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required.");
        }
        if (request.getOption() == null || request.getOption().isBlank()) {
            throw new IllegalArgumentException("Option is required.");
        }
    }

    private void applyReduceEmiKeepTenor(Loan loan, List<ScheduleEntry> schedule, int index, BigDecimal prepaymentAmount) {
        ScheduleEntry target = schedule.get(index);
        BigDecimal remainingPrincipal = target.getOpeningBalance().subtract(prepaymentAmount).setScale(4, RoundingMode.HALF_UP);
        int remainingMonths = schedule.size() - index;
        BigDecimal monthlyRate = loan.getAnnualInterestRate().divide(BigDecimal.valueOf(12), MathContext.DECIMAL128);
        BigDecimal onePlusRatePower = BigDecimal.ONE.add(monthlyRate).pow(remainingMonths, MathContext.DECIMAL128);
        BigDecimal newEmi = remainingPrincipal.multiply(monthlyRate).multiply(onePlusRatePower)
                .divide(onePlusRatePower.subtract(BigDecimal.ONE), 8, RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
        loan.setMonthlyEmi(newEmi);
        rebuildScheduleFromIndex(loan, schedule, index, remainingPrincipal, newEmi);
    }

    private void applyKeepEmiReduceTenor(Loan loan, List<ScheduleEntry> schedule, int index, BigDecimal prepaymentAmount) {
        ScheduleEntry target = schedule.get(index);
        BigDecimal remainingPrincipal = target.getOpeningBalance().subtract(prepaymentAmount).setScale(4, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = loan.getAnnualInterestRate().divide(BigDecimal.valueOf(12), MathContext.DECIMAL128);
        BigDecimal numerator = monthlyRate.multiply(remainingPrincipal);
        BigDecimal denominator = loan.getMonthlyEmi().subtract(remainingPrincipal.multiply(monthlyRate));
        if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot keep EMI constant with this prepayment amount.");
        }
        double months = Math.log(loan.getMonthlyEmi().doubleValue() / denominator.doubleValue()) / Math.log(1 + monthlyRate.doubleValue());
        int newMonths = (int) Math.ceil(months);
        rebuildScheduleFromIndexWithReducedTenor(loan, schedule, index, remainingPrincipal, loan.getMonthlyEmi(), newMonths);
    }

    private void applyAdvanceInstallments(Loan loan, List<ScheduleEntry> schedule, int index, BigDecimal prepaymentAmount) {
        for (int i = 0; i <= index; i++) {
            ScheduleEntry entry = schedule.get(i);
            if (entry.getStatus().equals("PENDING")) {
                entry.setStatus("PAID");
                entry.setPaidDate(LocalDate.now());
            }
        }
        loan.setMonthlyEmi(loan.getMonthlyEmi());
    }

    private void rebuildScheduleFromIndex(Loan loan, List<ScheduleEntry> schedule, int startIndex, BigDecimal openingBalance, BigDecimal emi) {
        LocalDate dueDate = schedule.get(startIndex).getDueDate();
        for (int i = startIndex; i < schedule.size(); i++) {
            int installmentNumber = i + 1;
            BigDecimal interest = openingBalance.multiply(loan.getAnnualInterestRate().divide(BigDecimal.valueOf(12), MathContext.DECIMAL128)).setScale(4, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interest).setScale(4, RoundingMode.HALF_UP);
            BigDecimal closingBalance = openingBalance.subtract(principalComponent).setScale(4, RoundingMode.HALF_UP);
            if (i == schedule.size() - 1) {
                closingBalance = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
                principalComponent = openingBalance;
                emi = interest.add(principalComponent).setScale(4, RoundingMode.HALF_UP);
            }
            ScheduleEntry entry = schedule.get(i);
            entry.setOpeningBalance(openingBalance);
            entry.setEmiAmount(emi);
            entry.setPrincipalComponent(principalComponent);
            entry.setInterestComponent(interest);
            entry.setClosingBalance(closingBalance);
            entry.setDueDate(dueDate.plusMonths(i - startIndex));
            openingBalance = closingBalance;
        }
        scheduleEntryRepository.saveAll(schedule);
    }

    private void rebuildScheduleFromIndexWithReducedTenor(Loan loan, List<ScheduleEntry> schedule, int startIndex, BigDecimal openingBalance, BigDecimal emi, int months) {
        List<ScheduleEntry> truncated = new java.util.ArrayList<>(schedule.subList(0, startIndex));
        LocalDate dueDate = schedule.get(startIndex).getDueDate();
        for (int i = 0; i < months; i++) {
            BigDecimal interest = openingBalance.multiply(loan.getAnnualInterestRate().divide(BigDecimal.valueOf(12), MathContext.DECIMAL128)).setScale(4, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interest).setScale(4, RoundingMode.HALF_UP);
            BigDecimal closingBalance = openingBalance.subtract(principalComponent).setScale(4, RoundingMode.HALF_UP);
            if (i == months - 1) {
                closingBalance = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
                principalComponent = openingBalance;
                emi = interest.add(principalComponent).setScale(4, RoundingMode.HALF_UP);
            }
            ScheduleEntry entry = new ScheduleEntry(loan, startIndex + i + 1, dueDate.plusMonths(i), openingBalance, emi, principalComponent, interest, closingBalance, "PENDING");
            truncated.add(entry);
            openingBalance = closingBalance;
        }
        List<ScheduleEntry> toDelete = new java.util.ArrayList<>(schedule.subList(startIndex, schedule.size()));
        scheduleEntryRepository.deleteAll(toDelete);
        schedule = scheduleEntryRepository.saveAll(truncated);
        // Update the loan's scheduleEntries to reflect the new saved entries
        loan.setScheduleEntries(schedule);
        loan.setTenorMonths(startIndex + months);
    }

    public SettlementResponse calculateSettlement(Long loanId, SettlementRequest request) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new EntityNotFoundException("Loan not found"));
        validateSettlementRequest(request);

        List<ScheduleEntry> schedule = scheduleEntryRepository.findByLoanIdOrderByInstallmentNumber(loanId);
        int targetIndex = request.getInstallmentNumber() - 1;
        if (targetIndex < 0 || targetIndex >= schedule.size()) {
            throw new IllegalArgumentException("Invalid installment number.");
        }
        ScheduleEntry targetEntry = schedule.get(targetIndex);
        BigDecimal outstandingPrincipal = targetEntry.getOpeningBalance();
        BigDecimal currentMonthInterest = targetEntry.getInterestComponent();
        BigDecimal payoffAmount;
        String option = request.getOption();
        switch (option) {
            case "D" -> payoffAmount = outstandingPrincipal.add(currentMonthInterest).setScale(4, RoundingMode.HALF_UP);
            case "E" -> payoffAmount = calculateRuleOf78Payoff(schedule, targetIndex).setScale(4, RoundingMode.HALF_UP);
            case "F" -> payoffAmount = outstandingPrincipal.add(BigDecimal.valueOf(20000)).setScale(4, RoundingMode.HALF_UP);
            default -> throw new IllegalArgumentException("Unsupported settlement option.");
        }

        if (Boolean.TRUE.equals(request.getApply())) {
            loan.setStatus("SETTLED");
            loanRepository.save(loan);
            transactionRepository.save(new LoanTransaction(loan, "SETTLEMENT", option, payoffAmount, request.getInstallmentNumber(), LocalDateTime.now(), "Applied settlement option " + option));
        }

        return new SettlementResponse(option, payoffAmount, "Settlement quote for option " + option);
    }

    private void validateSettlementRequest(SettlementRequest request) {
        if (request.getInstallmentNumber() == null || request.getInstallmentNumber() <= 0) {
            throw new IllegalArgumentException("Installment number is required and must be positive.");
        }
        if (request.getOption() == null || request.getOption().isBlank()) {
            throw new IllegalArgumentException("Option is required.");
        }
    }

    private BigDecimal calculateRuleOf78Payoff(List<ScheduleEntry> schedule, int targetIndex) {
        int totalMonths = schedule.size();
        int remainingMonths = totalMonths - targetIndex;
        int totalSum = totalMonths * (totalMonths + 1) / 2;
        int remainingSum = remainingMonths * (remainingMonths + 1) / 2;
        BigDecimal remainingInterest = schedule.stream()
                .skip(targetIndex)
                .map(ScheduleEntry::getInterestComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weightedInterest = remainingInterest.multiply(BigDecimal.valueOf((double) remainingSum / totalSum));
        ScheduleEntry targetEntry = schedule.get(targetIndex);
        return targetEntry.getOpeningBalance().add(weightedInterest);
    }
}
