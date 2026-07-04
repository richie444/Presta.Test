package com.example.loanengine.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ScheduleEntryResponse {
    private Long id;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal openingBalance;
    private BigDecimal emiAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal closingBalance;
    private String status;
    private LocalDate paidDate;

    public ScheduleEntryResponse(Long id, Integer installmentNumber, LocalDate dueDate, BigDecimal openingBalance, BigDecimal emiAmount, BigDecimal principalComponent, BigDecimal interestComponent, BigDecimal closingBalance, String status, LocalDate paidDate) {
        this.id = id;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.openingBalance = openingBalance;
        this.emiAmount = emiAmount;
        this.principalComponent = principalComponent;
        this.interestComponent = interestComponent;
        this.closingBalance = closingBalance;
        this.status = status;
        this.paidDate = paidDate;
    }

    public Long getId() { return id; }
    public Integer getInstallmentNumber() { return installmentNumber; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public BigDecimal getEmiAmount() { return emiAmount; }
    public BigDecimal getPrincipalComponent() { return principalComponent; }
    public BigDecimal getInterestComponent() { return interestComponent; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public String getStatus() { return status; }
    public LocalDate getPaidDate() { return paidDate; }
}
