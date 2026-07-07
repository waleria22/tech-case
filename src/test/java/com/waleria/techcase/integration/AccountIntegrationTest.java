package com.waleria.techcase.integration;

import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.assertj.core.api.Assertions.assertThat;

public class AccountIntegrationTest extends  BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void shouldCreateAccountEndToEnd() {
        AccountDTORequest accountRequest = new AccountDTORequest("12345678900");
        ResponseEntity<AccountDTOResponse> response =
                restTemplate.postForEntity("/accounts", accountRequest, AccountDTOResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountId()).isNotNull();
        assertThat(response.getBody().getDocumentNumber()).isEqualTo("12345678900");
    }

    @Test
    void shouldReturnAccountById() {
        AccountDTORequest request = new AccountDTORequest("12345678900");

        ResponseEntity<AccountDTOResponse> createResponse =
                restTemplate.postForEntity(
                        "/accounts",
                        request,
                        AccountDTOResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long accountId = createResponse.getBody().getAccountId();

        ResponseEntity<AccountDTOResponse> getResponse =
                restTemplate.getForEntity(
                        "/accounts/" + accountId,
                        AccountDTOResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getAccountId()).isEqualTo(accountId);
        assertThat(getResponse.getBody().getDocumentNumber()).isEqualTo("12345678900");
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/accounts/999999",
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
