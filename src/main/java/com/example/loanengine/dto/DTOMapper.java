package com.example.loanengine.dto;

import com.example.loanengine.domain.Loan;
import com.example.loanengine.domain.ScheduleEntry;
import com.example.loanengine.domain.LoanTransaction;

import java.util.List;
import java.util.stream.Collectors;

public class DTOMapper {

    public static ScheduleEntryResponse toScheduleResponse(ScheduleEntry e) {
        if (e == null) return null;
        return new ScheduleEntryResponse(e.getId(), e.getInstallmentNumber(), e.getDueDate(), e.getOpeningBalance(), e.getEmiAmount(), e.getPrincipalComponent(), e.getInterestComponent(), e.getClosingBalance(), e.getStatus(), e.getPaidDate());
    }

    public static LoanTransactionResponse toTransactionResponse(LoanTransaction t) {
        if (t == null) return null;
        return new LoanTransactionResponse(t.getId(), t.getTransactionType(), t.getStrategy(), t.getAmount(), t.getInstallmentNumber(), t.getTransactionDate(), t.getDescription());
    }

    public static LoanResponse toLoanResponse(Loan loan) {
        if (loan == null) return null;
        List<ScheduleEntryResponse> schedule = loan.getScheduleEntries() == null ? List.of() : loan.getScheduleEntries().stream().map(DTOMapper::toScheduleResponse).collect(Collectors.toList());
        List<LoanTransactionResponse> txs = loan.getTransactions() == null ? List.of() : loan.getTransactions().stream().map(DTOMapper::toTransactionResponse).collect(Collectors.toList());
        return new LoanResponse(loan.getId(), loan.getProductName(), loan.getPrincipal(), loan.getAnnualInterestRate(), loan.getTenorMonths(), loan.getMonthlyEmi(), loan.getStartDate(), loan.getStatus(), schedule, txs);
    }
}
