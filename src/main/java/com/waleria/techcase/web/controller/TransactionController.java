package com.waleria.techcase.web.controller;

import com.waleria.techcase.service.TransactionService;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;


    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

   @PostMapping
    public ResponseEntity<TransactionDTOResponse> createTransaction(@Valid @RequestBody TransactionDTORequest request) {
       TransactionDTOResponse response = transactionService.createTransaction(request);
       return ResponseEntity.status(201).body(response);

    }

}
