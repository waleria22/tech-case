package com.waleria.techcase.web.controller;

import com.waleria.techcase.service.AccountService;
import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Accounts", description = "Card account-related operations")
@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create a new account", description = "Creates an account associated with a document number.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    @PostMapping
    public ResponseEntity<AccountDTOResponse> createAccount(@Valid @RequestBody AccountDTORequest dto) {
      AccountDTOResponse account =  accountService.createAccount(dto);
      return ResponseEntity.status(201).body(account);
    }


    @Operation(summary = "Search for an account", description = "Searches for an account based on the accountId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTOResponse> retrieveAccount(@Valid @PathVariable Long accountId) {
        AccountDTOResponse account = accountService.retrieveAccount(accountId);
        return ResponseEntity.ok(account);

    }
}
