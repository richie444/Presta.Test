package com.example.loanengine.repository;

import com.example.loanengine.domain.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {
}
