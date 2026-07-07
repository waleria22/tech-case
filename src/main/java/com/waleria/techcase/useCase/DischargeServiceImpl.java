package com.waleria.techcase.useCase;

import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.service.DischargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DischargeServiceImpl implements DischargeService {

    private final TransactionRepository transactionRepository;

    @Override
    public BigDecimal applyCreditToDebits(Long accountId, BigDecimal creditAmount) {
        List<TransactionEntity> debitTransactions =
                getDebitTransactions(accountId);

        BigDecimal remainingCredit = creditAmount;

        for (TransactionEntity transaction : debitTransactions) {
            if (remainingCredit.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal debt = transaction.getBalance().abs();

            // Crédito suficiente para quitar toda a dívida
            if (remainingCredit.compareTo(debt) >= 0) {

                transaction.setBalance(BigDecimal.ZERO);

                remainingCredit = remainingCredit.subtract(debt);

            } else {

                // Quita parcialmente
                transaction.setBalance(
                        transaction.getBalance().add(remainingCredit)
                );

                remainingCredit = BigDecimal.ZERO;
            }

            transactionRepository.save(transaction);
        }


        return remainingCredit;
    }

    private @NonNull List<TransactionEntity> getDebitTransactions(Long accountId) {
        return transactionRepository.findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(accountId, BigDecimal.ZERO)
                .stream()
                .filter(it -> OperationType.fromId(it.getOperationTypeId()).isDebit())
                .toList();
    }
}
