package com.waleria.techcase;

import com.waleria.techcase.repository.AccountEntity;
import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.useCase.TransactionServiceImpl;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import com.waleria.techcase.web.exception.OperationTypeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

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

    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        accountEntity = new AccountEntity();
        accountEntity.setAccountId(1L);
        accountEntity.setDocumentNumber("12345678900");
    }

    @Test
    void shouldCreateCreditTransactionWithPositiveAmount() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 4L, new BigDecimal("60.00"));

        TransactionEntity saved = new TransactionEntity();
        saved.setId(1L);
        saved.setAccount(accountEntity);
        saved.setOperationTypeId(4L);
        saved.setAmount(new BigDecimal("60.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(saved);

        TransactionDTOResponse response = transactionService.createTransaction(request);

        assertThat(response.getAmount()).isEqualByComparingTo("60.00");
        assertThat(response.getOperationTypeId()).isEqualTo(4L);
    }

    @Test
    void shouldNormalizeAmountToNegativeWhenNormalPurchase() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 1L, new BigDecimal("50.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("-50.00");
    }

    @Test
    void shouldNormalizeAmountToNegativeEvenWhenAmountIsPositiveForWithdrawal() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 3L, new BigDecimal("100.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("-100.00");
    }

    @Test
    void shouldThrowExceptionWhenOperationTypeIdDoesNotExist() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 999L, new BigDecimal("10.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(OperationTypeNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldSetEventDateAutomatically() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 4L, new BigDecimal("20.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getEventDate()).isNotNull();
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        TransactionDTORequest request = new TransactionDTORequest(999999L, 4L, new BigDecimal("50.00"));

        when(accountRepository.findById(999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldLinkTransactionToCorrectAccount() {
        TransactionDTORequest request = new TransactionDTORequest(1L, 4L, new BigDecimal("30.00"));

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.createTransaction(request);

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAccount()).isEqualTo(accountEntity);
        assertThat(captor.getValue().getAccount().getAccountId()).isEqualTo(1L);
    }
}