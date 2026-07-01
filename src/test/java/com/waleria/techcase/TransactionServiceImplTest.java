package com.waleria.techcase;

import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.useCase.TransactionServiceImpl;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import com.waleria.techcase.web.exception.OperationTypeNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldCreateCreditTransactionWithPositiveAmount() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 4L, new BigDecimal("60.00"));

        TransactionEntity saved = new TransactionEntity();
        saved.setId(1L);
        saved.setAccountId(1L);
        saved.setOperationTypeId(4L);
        saved.setAmount(new BigDecimal("60.00"));

        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(saved);

        TransactionDTOResponse response = transactionService.createTransaction(request);

        assertThat(response.getAmount()).isEqualByComparingTo("60.00");
        assertThat(response.getOperationTypeId()).isEqualTo(4L);
    }

    @Test
    void shouldNormalizeAmountToNegativeWhenNormalPurchase() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 1L, new BigDecimal("50.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("-50.00");
    }

    @Test
    void shouldNormalizeAmountToNegativeEvenWhenAmountIsPositiveForWithdrawal() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 3L, new BigDecimal("100.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("-100.00");
    }

    @Test
    void shouldThrowExceptionWhenOperationTypeIdDoesNotExist() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 999L, new BigDecimal("10.00"));

        when(accountRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(OperationTypeNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldSetEventDateAutomatically() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 4L, new BigDecimal("20.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getEventDate()).isNotNull();
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        TransactionDTORequest request = new TransactionDTORequest(999999L, 4L, new BigDecimal("50.00"));

        when(accountRepository.existsById(999999L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }
}