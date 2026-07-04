package com.example.loanengine.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanTransactionResponse {
    private Long id;
    private String transactionType;
    private String strategy;
    private BigDecimal amount;
    private Integer installmentNumber;
    private LocalDateTime transactionDate;
    private String description;

    public LoanTransactionResponse(Long id, String transactionType, String strategy, BigDecimal amount, Integer installmentNumber, LocalDateTime transactionDate, String description) {
        this.id = id;
        this.transactionType = transactionType;
        this.strategy = strategy;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getTransactionType() { return transactionType; }
    public String getStrategy() { return strategy; }
    public java.math.BigDecimal getAmount() { return amount; }
    public Integer getInstallmentNumber() { return installmentNumber; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getDescription() { return description; }
}
