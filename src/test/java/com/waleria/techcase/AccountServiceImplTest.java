package com.waleria.techcase;


import com.waleria.techcase.repository.AccountEntity;
import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.useCase.AccountServiceImpl;
import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        accountEntity = new AccountEntity();
        accountEntity.setAccountId(1L);
        accountEntity.setDocumentNumber("12345678900");
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        AccountDTORequest request = new AccountDTORequest("12345678900");

        when(accountRepository.save(any(AccountEntity.class))).thenReturn(accountEntity);

        AccountDTOResponse response = accountService.createAccount(request);

        assertThat(response.getAccountId()).isEqualTo(1L);
        assertThat(response.getDocumentNumber()).isEqualTo("12345678900");

        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void shouldCallSaveWithCorrectDocumentNumber() {
        AccountDTORequest request = new AccountDTORequest("98765432100");
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(accountEntity);

        accountService.createAccount(request);

        verify(accountRepository).save(argThat(entity ->
                entity.getDocumentNumber().equals("98765432100")
        ));
    }

    @Test
    void shouldReturnAccountWhenAccountIdExists() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));

        AccountDTOResponse response = accountService.retrieveAccount(1L);

        assertThat(response.getAccountId()).isEqualTo(1L);
        assertThat(response.getDocumentNumber()).isEqualTo("12345678900");
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        Long id = 999L;
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.retrieveAccount(id))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("999");

        verify(accountRepository, times(1)).findById(id);
    }
}
