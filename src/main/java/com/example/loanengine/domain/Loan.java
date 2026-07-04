package com.example.loanengine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principal;

    @Column(name = "annual_interest_rate", nullable = false, precision = 9, scale = 6)
    private BigDecimal annualInterestRate;

    @Column(name = "tenor_months", nullable = false)
    private Integer tenorMonths;

    @Column(name = "monthly_emi", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyEmi;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private String status;

    @JsonManagedReference("schedule")
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEntry> scheduleEntries = new ArrayList<>();

    @JsonManagedReference("transactions")
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanTransaction> transactions = new ArrayList<>();

    public Loan() {
    }

    public Loan(String productName, BigDecimal principal, BigDecimal annualInterestRate, Integer tenorMonths, BigDecimal monthlyEmi, LocalDate startDate, String status) {
        this.productName = productName;
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.tenorMonths = tenorMonths;
        this.monthlyEmi = monthlyEmi;
        this.startDate = startDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public Integer getTenorMonths() {
        return tenorMonths;
    }

    public void setTenorMonths(Integer tenorMonths) {
        this.tenorMonths = tenorMonths;
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(BigDecimal monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ScheduleEntry> getScheduleEntries() {
        return scheduleEntries;
    }

    public void setScheduleEntries(List<ScheduleEntry> scheduleEntries) {
        this.scheduleEntries = scheduleEntries;
    }

    public List<LoanTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<LoanTransaction> transactions) {
        this.transactions = transactions;
    }
}
