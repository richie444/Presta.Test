package com.example.loanengine.dto;

import java.math.BigDecimal;

public class SettlementResponse {
    private String option;
    private BigDecimal payoffAmount;
    private String description;

    public SettlementResponse(String option, BigDecimal payoffAmount, String description) {
        this.option = option;
        this.payoffAmount = payoffAmount;
        this.description = description;
    }

    public String getOption() {
        return option;
    }

    public BigDecimal getPayoffAmount() {
        return payoffAmount;
    }

    public String getDescription() {
        return description;
    }
}
