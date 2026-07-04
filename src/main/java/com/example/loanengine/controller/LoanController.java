package com.example.loanengine.controller;

import com.example.loanengine.domain.Loan;
import com.example.loanengine.domain.ScheduleEntry;
import com.example.loanengine.dto.DTOMapper;
import com.example.loanengine.dto.LoanResponse;
import com.example.loanengine.dto.ScheduleEntryResponse;
import com.example.loanengine.dto.LoanRequest;
import com.example.loanengine.dto.PrepaymentRequest;
import com.example.loanengine.dto.SettlementRequest;
import com.example.loanengine.dto.SettlementResponse;
import com.example.loanengine.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanRequest request) {
        Loan loan = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DTOMapper.toLoanResponse(loan));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<List<ScheduleEntryResponse>> getSchedule(@PathVariable Long loanId) {
        List<ScheduleEntry> list = loanService.getSchedule(loanId);
        List<ScheduleEntryResponse> dto = list.stream().map(DTOMapper::toScheduleResponse).toList();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{loanId}/prepayments")
    public ResponseEntity<Void> postPrepayment(@PathVariable Long loanId, @RequestBody PrepaymentRequest request) {
        loanService.processPrepayment(loanId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{loanId}/settlements")
    public ResponseEntity<SettlementResponse> postSettlement(@PathVariable Long loanId, @RequestBody SettlementRequest request) {
        return ResponseEntity.ok(loanService.calculateSettlement(loanId, request));
    }
}
