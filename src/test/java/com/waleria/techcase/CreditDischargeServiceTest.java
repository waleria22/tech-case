package com.waleria.techcase;

import com.waleria.techcase.repository.AccountEntity;
import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.useCase.DischargeServiceImpl;
import com.waleria.techcase.useCase.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditDischargeServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DischargeServiceImpl dischargeService;

    private AccountEntity account;

    @BeforeEach
    void setUp() {
        account = new AccountEntity();
        account.setAccountId(1L);
        account.setDocumentNumber("12345678900");
    }

    private TransactionEntity debit(BigDecimal balance, Long operationTypeId, LocalDateTime eventDate) {
        TransactionEntity tx = new TransactionEntity();
        tx.setAccount(account);
        tx.setOperationTypeId(operationTypeId);
        tx.setBalance(balance);
        tx.setEventDate(eventDate);
        return tx;
    }

    @Test
    void shouldFullySettleSingleDebtWhenCreditIsExactlyEqual() {
        TransactionEntity purchase = debit(new BigDecimal("-50.00"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));

        when(transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(1L, BigDecimal.ZERO))
                .thenReturn(List.of(purchase));

        BigDecimal remaining = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("50.00"));

        assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(purchase.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(transactionRepository).save(purchase);
    }

    @Test
    void shouldPartiallySettleDebtWhenCreditIsLessThanDebt() {
        TransactionEntity firstPurchase = debit(new BigDecimal("-50.00"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));
        TransactionEntity secondPurchase = debit(new BigDecimal("-23.50"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));
        TransactionEntity thirdPurchase = debit(new BigDecimal("-18.7"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));
        List<TransactionEntity> debitTransactions = new java.util.ArrayList<>(List.of());
        debitTransactions.add(firstPurchase);
        debitTransactions.add(secondPurchase);
        debitTransactions.add(thirdPurchase);

        when(transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(account.getAccountId(), BigDecimal.ZERO))
                .thenReturn(debitTransactions);

        BigDecimal remaining = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("60.00"));

        assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(firstPurchase.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(secondPurchase.getBalance()).isEqualByComparingTo("-13.5");
        assertThat(thirdPurchase.getBalance()).isEqualByComparingTo("-18.70");
        verify(transactionRepository).save(firstPurchase);
    }

    @Test
    void shouldReturnRemainingCreditWhenGreaterThanTotalDebt() {
        TransactionEntity firstPurchase = debit(new BigDecimal("-50.00"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));
        TransactionEntity secondPurchase = debit(new BigDecimal("-23.50"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));
        TransactionEntity thirdPurchase = debit(new BigDecimal("-18.7"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));
        List<TransactionEntity> debitTransactions = new java.util.ArrayList<>(List.of());
        debitTransactions.add(firstPurchase);
        debitTransactions.add(secondPurchase);
        debitTransactions.add(thirdPurchase);

        when(transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(account.getAccountId(), BigDecimal.ZERO))
                .thenReturn(debitTransactions);

        BigDecimal remaining = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("60.00"));
        BigDecimal remaining2 = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("100.00"));

        assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(remaining2).isEqualByComparingTo("67.8");
        assertThat(firstPurchase.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(secondPurchase.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(thirdPurchase.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnFullAmountWhenThereAreNoOutstandingDebts() {
        when(transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(account.getAccountId(), BigDecimal.ZERO))
                .thenReturn(List.of());

        BigDecimal remaining = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("100.00"));

        assertThat(remaining).isEqualByComparingTo("100.00");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldSettleMultipleDebtsInOrderFromOldestToNewest() {
        TransactionEntity oldest = debit(new BigDecimal("-30.00"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(3));
        TransactionEntity newest = debit(new BigDecimal("-50.00"), OperationType.WITHDRAWAL.getId(), LocalDateTime.now().minusDays(1));

        when(transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(account.getAccountId(), BigDecimal.ZERO))
                .thenReturn(List.of(oldest, newest));

        BigDecimal remaining = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("40.00"));

        assertThat(oldest.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newest.getBalance()).isEqualByComparingTo("-40.00");
        assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldExcludeCreditVoucherTransactionsFromDischarge() {
        TransactionEntity creditVoucherWithNegativeBalance = debit(new BigDecimal("-10.00"), OperationType.CREDIT_VOUCHER.getId(), LocalDateTime.now());
        TransactionEntity purchase = debit(new BigDecimal("-20.00"), OperationType.NORMAL_PURCHASE.getId(), LocalDateTime.now().minusDays(1));

        when(transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(account.getAccountId(), BigDecimal.ZERO))
                .thenReturn(List.of(creditVoucherWithNegativeBalance, purchase));

        BigDecimal remaining = dischargeService.applyCreditToDebits(account.getAccountId(), new BigDecimal("20.00"));

        assertThat(purchase.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(creditVoucherWithNegativeBalance.getBalance()).isEqualByComparingTo("-10.00");
        assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
    }
}