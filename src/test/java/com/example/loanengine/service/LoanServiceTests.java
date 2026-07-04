package com.example.loanengine.service;

import com.example.loanengine.domain.Loan;
import com.example.loanengine.domain.ScheduleEntry;
import com.example.loanengine.dto.LoanRequest;
import com.example.loanengine.dto.PrepaymentRequest;
import com.example.loanengine.dto.SettlementRequest;
import com.example.loanengine.dto.SettlementResponse;
import com.example.loanengine.repository.LoanRepository;
import com.example.loanengine.repository.LoanTransactionRepository;
import com.example.loanengine.repository.ScheduleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(LoanService.class)
@org.springframework.test.context.ActiveProfiles("test")
public class LoanServiceTests {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Autowired
    private LoanTransactionRepository transactionRepository;

    @Autowired
    private LoanService loanService;

    private Loan loan;

    @BeforeEach
    void setUp() {
        LoanRequest request = new LoanRequest();
        request.setProductName("Retail Personal Loan");
        request.setPrincipal(new BigDecimal("1000000"));
        request.setAnnualInterestRate(new BigDecimal("0.12"));
        request.setTenorMonths(60);
        request.setStartDate(LocalDate.of(2026, 1, 1));
        loan = loanService.createLoan(request);
    }

    @Test
    void createLoanGeneratesSchedule() {
        assertThat(loan.getMonthlyEmi()).isNotNull();
        List<ScheduleEntry> schedule = loanService.getSchedule(loan.getId());
        assertThat(schedule).hasSize(60);
        assertThat(schedule.get(0).getOpeningBalance()).isEqualByComparingTo("1000000.0000");
    }

    @Test
    void generatedScheduleMatchesSampleDataForFirst24Rows() {
        LoanRequest request = new LoanRequest();
        request.setProductName("Retail Personal Loan");
        request.setPrincipal(new BigDecimal("1000000"));
        request.setAnnualInterestRate(new BigDecimal("0.12"));
        request.setTenorMonths(60);
        request.setStartDate(LocalDate.of(2024, 7, 24));
        Loan sampleLoan = loanService.createLoan(request);
        List<ScheduleEntry> schedule = loanService.getSchedule(sampleLoan.getId());

        assertThat(sampleLoan.getMonthlyEmi()).isEqualByComparingTo("22244.45");
        assertThat(schedule.get(0).getInterestComponent()).isEqualByComparingTo("10000.00");
        assertThat(schedule.get(0).getPrincipalComponent()).isEqualByComparingTo("12244.45");
        assertThat(schedule.get(1).getInterestComponent()).isEqualByComparingTo("9877.56");
        assertThat(schedule.get(1).getPrincipalComponent()).isEqualByComparingTo("12366.89");
        assertThat(schedule.get(23).getInterestComponent()).isEqualByComparingTo("6851.18");
        assertThat(schedule.get(23).getPrincipalComponent()).isEqualByComparingTo("15393.27");
        assertThat(schedule.get(23).getClosingBalance()).isEqualByComparingTo("669724.76");
    }

    @Test
    void prepaymentOptionAReducesEmiAndKeepsTenor() {
        PrepaymentRequest request = new PrepaymentRequest();
        request.setInstallmentNumber(24);
        request.setAmount(new BigDecimal("200000"));
        request.setOption("A");

        loanService.processPrepayment(loan.getId(), request);
        Loan updated = loanRepository.findById(loan.getId()).orElseThrow();
        assertThat(updated.getMonthlyEmi()).isLessThan(new BigDecimal("22244"));
        assertThat(updated.getTenorMonths()).isEqualTo(60);
    }

    @Test
    void prepaymentOptionBKeepsEmiAndReducesTenor() {
        PrepaymentRequest request = new PrepaymentRequest();
        request.setInstallmentNumber(24);
        request.setAmount(new BigDecimal("200000"));
        request.setOption("B");

        loanService.processPrepayment(loan.getId(), request);
        Loan updated = loanRepository.findById(loan.getId()).orElseThrow();
        assertThat(updated.getMonthlyEmi()).isEqualByComparingTo(loan.getMonthlyEmi());
        assertThat(updated.getTenorMonths()).isLessThan(60);
    }

    @Test
    void prepaymentOptionCMarksPaid() {
        PrepaymentRequest request = new PrepaymentRequest();
        request.setInstallmentNumber(24);
        request.setAmount(new BigDecimal("200000"));
        request.setOption("C");

        loanService.processPrepayment(loan.getId(), request);
        List<ScheduleEntry> schedule = loanService.getSchedule(loan.getId());
        assertThat(schedule.get(23).getStatus()).isEqualTo("PAID");
    }

    @Test
    void settlementOptionDReturnsTruePayoff() {
        SettlementRequest request = new SettlementRequest();
        request.setInstallmentNumber(24);
        request.setOption("D");
        request.setApply(false);

        SettlementResponse response = loanService.calculateSettlement(loan.getId(), request);
        assertThat(response.getPayoffAmount()).isGreaterThan(new BigDecimal("680000"));
    }

    @Test
    void invalidPrepaymentThrows() {
        PrepaymentRequest request = new PrepaymentRequest();
        request.setInstallmentNumber(100);
        request.setAmount(new BigDecimal("1000"));
        request.setOption("A");

        assertThatThrownBy(() -> loanService.processPrepayment(loan.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
