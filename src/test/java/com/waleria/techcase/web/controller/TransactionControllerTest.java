package com.waleria.techcase.web.controller;

import com.waleria.techcase.service.TransactionService;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import com.waleria.techcase.web.exception.GlobalExceptionHandler;
import com.waleria.techcase.web.exception.OperationTypeNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldReturn201WhenTransactionIsCreated() throws Exception {
        TransactionDTOResponse response = new TransactionDTOResponse(
                1L, 1L, 4L, new BigDecimal("60.00"), new BigDecimal("60.00"));

        when(transactionService.createTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"account_id": 1, "operation_type_id": 4, "amount": 60.00}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.amount").value(60.00))
                .andExpect(jsonPath("$.balance").value(60.00));
    }

    @Test
    void shouldReturn400WhenAccountIdIsMissing() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"operation_type_id": 4, "amount": 60.00}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenOperationTypeIdIsMissing() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"account_id": 1, "amount": 60.00}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAmountIsMissing() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"account_id": 1, "operation_type_id": 4}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"account_id": 999, "operation_type_id": 4, "amount": 50.00}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: 999"));
    }

    @Test
    void shouldReturn404WhenOperationTypeIsInvalid() throws Exception {
        when(transactionService.createTransaction(any()))
                .thenThrow(new OperationTypeNotFoundException(99L));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"account_id": 1, "operation_type_id": 99, "amount": 50.00}
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WithTransactionListWhenAccountHasTransactions() throws Exception {
        TransactionDTOResponse tx1 = new TransactionDTOResponse(
                1L, 1L, 1L, new BigDecimal("-50.00"), new BigDecimal("0.00"));
        TransactionDTOResponse tx2 = new TransactionDTOResponse(
                2L, 1L, 4L, new BigDecimal("60.00"), new BigDecimal("10.00"));

        when(transactionService.listTransactionsByAccount(1L)).thenReturn(List.of(tx1, tx2));

        mockMvc.perform(get("/transactions").param("account_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionId").value(1))
                .andExpect(jsonPath("$[0].balance").value(0.00))
                .andExpect(jsonPath("$[1].transactionId").value(2))
                .andExpect(jsonPath("$[1].balance").value(10.00));
    }

    @Test
    void shouldReturn200WithEmptyListWhenAccountHasNoTransactions() throws Exception {
        when(transactionService.listTransactionsByAccount(1L)).thenReturn(List.of());

        mockMvc.perform(get("/transactions").param("account_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn404WhenListingTransactionsForNonExistentAccount() throws Exception {
        when(transactionService.listTransactionsByAccount(999L))
                .thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(get("/transactions").param("account_id", "999"))
                .andExpect(status().isNotFound());
    }
}