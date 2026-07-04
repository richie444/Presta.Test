package com.example.loanengine.dto;

import java.math.BigDecimal;

public class PrepaymentRequest {
    private Integer installmentNumber;
    private BigDecimal amount;
    private String option;

    public PrepaymentRequest() {
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
