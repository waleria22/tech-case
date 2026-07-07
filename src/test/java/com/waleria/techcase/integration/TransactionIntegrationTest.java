package com.waleria.techcase.integration;

import com.waleria.techcase.useCase.OperationType;
import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private Long accountId;

    @BeforeEach
    void setUp() {
        AccountDTORequest accountRequest = new AccountDTORequest("12345678900");
        ResponseEntity<AccountDTOResponse> response =
                restTemplate.postForEntity("/accounts", accountRequest, AccountDTOResponse.class);
        accountId = response.getBody().getAccountId();
    }

    private TransactionDTOResponse createTransaction(Long operationTypeId, BigDecimal amount) {
        TransactionDTORequest request = new TransactionDTORequest(accountId, operationTypeId, amount);
        ResponseEntity<TransactionDTOResponse> response =
                restTemplate.postForEntity("/transactions", request, TransactionDTOResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private List<TransactionDTOResponse> listTransactions() {
        ResponseEntity<List<TransactionDTOResponse>> response = restTemplate.exchange(
                "/transactions?account_id=" + accountId,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }


    @Test
    void shouldCreateTransactionEndToEnd() {
        TransactionDTOResponse transaction = createTransaction(OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("150.00"));
        assertThat(transaction.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldReturn404WhenCreatingTransactionForNonExistentAccount() {
        TransactionDTORequest txRequest = new TransactionDTORequest(999999L,  OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("50.00"));

        ResponseEntity<String> response =
                restTemplate.postForEntity("/transactions", txRequest, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldReturn404WhenCreatingTransactionForNonExistentOperationType() {
        TransactionDTORequest txRequest = new TransactionDTORequest(accountId,  10L, new BigDecimal("50.00"));

        ResponseEntity<String> response =
                restTemplate.postForEntity("/transactions", txRequest, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldNormalizeWithdrawalAmountToNegativeAgainstRealDatabase() {

        TransactionDTOResponse response = createTransaction(OperationType.WITHDRAWAL.getId(), new BigDecimal("80.00"));
        assertThat(response.getAmount()).isEqualByComparingTo("-80.00");
    }


    @Test
    void shouldDischargeOldestDebtsFirstAndLeaveRemainingUntouchedWhenCreditIsNotEnough() {
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("50.0"));
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("23.5"));
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("18.7"));
        createTransaction(OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("60.0"));

        List<TransactionDTOResponse> transactions = listTransactions();

        assertThat(transactions).hasSize(4);
        assertThat(transactions.get(0).getBalance()).isEqualByComparingTo("0.0");
        assertThat(transactions.get(1).getBalance()).isEqualByComparingTo("-13.5");
        assertThat(transactions.get(2).getBalance()).isEqualByComparingTo("-18.7");
        assertThat(transactions.get(3).getBalance()).isEqualByComparingTo("0.0");
    }


    @Test
    void shouldLeavePositiveRemainingBalanceOnPaymentWhenCreditExceedsAllDebts() {
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("50.0"));
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("23.5"));
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("18.7"));
        createTransaction(OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("60.0"));
        createTransaction(OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("100.0"));
        createTransaction(OperationType.NORMAL_PURCHASE.getId(), new BigDecimal("50.0"));

        List<TransactionDTOResponse> transactions = listTransactions();

        assertThat(transactions.get(0).getBalance()).isEqualByComparingTo("0.0");
        assertThat(transactions.get(1).getBalance()).isEqualByComparingTo("0.0");
        assertThat(transactions.get(2).getBalance()).isEqualByComparingTo("0.0");
        assertThat(transactions.get(3).getBalance()).isEqualByComparingTo("0.0");
        assertThat(transactions.get(4).getBalance()).isEqualByComparingTo("67.8");
        assertThat(transactions.get(5).getBalance()).isEqualByComparingTo("-50.0");
    }


    @Test
    void shouldNotDischargeCreditVoucherTransactionsAmongThemselves() {
        createTransaction(OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("30.0"));
        createTransaction(OperationType.CREDIT_VOUCHER.getId(), new BigDecimal("20.0"));
        List<TransactionDTOResponse> transactions = listTransactions();

        assertThat(transactions.get(0).getBalance()).isEqualByComparingTo("30.0");
        assertThat(transactions.get(1).getBalance()).isEqualByComparingTo("20.0");
    }

}