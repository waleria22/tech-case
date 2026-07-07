package com.waleria.techcase.web.controller;

import com.waleria.techcase.service.TransactionService;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transactions", description = "Operations related to card transactions")
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;


    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Creates a new transaction", description = "Creates a transaction related to a credit account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
   @PostMapping
    public ResponseEntity<TransactionDTOResponse> createTransaction(@Valid @RequestBody TransactionDTORequest request) {
       TransactionDTOResponse response = transactionService.createTransaction(request);
       return ResponseEntity.status(201).body(response);

    }

    @Operation(summary = "Lists transactions by account", description = "Returns all transactions for a given account, ordered by event date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of transactions returned successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping
    public ResponseEntity<List<TransactionDTOResponse>> listTransactionsByAccount(
            @RequestParam("account_id") Long accountId) {
        List<TransactionDTOResponse> response = transactionService.listTransactionsByAccount(accountId);
        return ResponseEntity.ok(response);
    }
}
