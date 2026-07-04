package com.example.loanengine.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LoanResponse {
    private Long id;
    private String productName;
    private BigDecimal principal;
    private BigDecimal annualInterestRate;
    private Integer tenorMonths;
    private BigDecimal monthlyEmi;
    private LocalDate startDate;
    private String status;
    private List<ScheduleEntryResponse> scheduleEntries;
    private List<LoanTransactionResponse> transactions;

    public LoanResponse(Long id, String productName, BigDecimal principal, BigDecimal annualInterestRate, Integer tenorMonths, BigDecimal monthlyEmi, LocalDate startDate, String status, List<ScheduleEntryResponse> scheduleEntries, List<LoanTransactionResponse> transactions) {
        this.id = id;
        this.productName = productName;
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.tenorMonths = tenorMonths;
        this.monthlyEmi = monthlyEmi;
        this.startDate = startDate;
        this.status = status;
        this.scheduleEntries = scheduleEntries;
        this.transactions = transactions;
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public BigDecimal getPrincipal() { return principal; }
    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public Integer getTenorMonths() { return tenorMonths; }
    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public LocalDate getStartDate() { return startDate; }
    public String getStatus() { return status; }
    public List<ScheduleEntryResponse> getScheduleEntries() { return scheduleEntries; }
    public List<LoanTransactionResponse> getTransactions() { return transactions; }
}
