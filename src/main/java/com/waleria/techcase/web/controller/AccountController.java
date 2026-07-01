package com.waleria.techcase.web.controller;

import com.waleria.techcase.service.AccountService;
import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountDTOResponse> createAccount(@Valid @RequestBody AccountDTORequest dto) {
      AccountDTOResponse account =  accountService.createAccount(dto);
      return ResponseEntity.status(201).body(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTOResponse> retrieveAccount(@Valid @PathVariable Long accountId) {
        AccountDTOResponse account = accountService.retrieveAccount(accountId);
        return ResponseEntity.ok(account);

    }
}
