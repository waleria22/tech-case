package com.waleria.techcase.web.controller;

import com.waleria.techcase.service.AccountService;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import com.waleria.techcase.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldReturn201WhenAccountIsCreated() throws Exception {
        AccountDTOResponse response = new AccountDTOResponse(1L, "12345678900");
        when(accountService.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"document_number": "12345678900"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void shouldReturn400WhenDocumentNumberIsMissing() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenAccountExists() throws Exception {
        AccountDTOResponse response = new AccountDTOResponse(1L, "12345678900");
        when(accountService.retrieveAccount(1L)).thenReturn(response);

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        when(accountService.retrieveAccount(999L)).thenThrow(new AccountNotFoundException(999L));

        mockMvc.perform(get("/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: 999"));
    }
}