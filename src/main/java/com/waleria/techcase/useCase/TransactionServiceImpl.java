package com.waleria.techcase.useCase;


import com.waleria.techcase.repository.AccountEntity;
import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.service.DischargeService;
import com.waleria.techcase.service.TransactionService;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final DischargeService dischargeService;

    @Transactional
    @Override
    public TransactionDTOResponse
    createTransaction(TransactionDTORequest request) {

        AccountEntity account = getAccount(request.getAccountId());
        OperationType operationType = OperationType.fromId(request.getOperationTypeId());
        BigDecimal normalizedAmount = operationType.normalize(request.getAmount());
        BigDecimal balance = resolveBalance(operationType, account.getAccountId(), normalizedAmount);

        TransactionEntity transactionEntity = buildTransaction(account, request, normalizedAmount, balance);

        TransactionEntity transactionSaved = transactionRepository.save(transactionEntity);

        return toResponse(transactionSaved);
    }

    @Override
    public List<TransactionDTOResponse> listTransactionsByAccount(Long accountId) {
       getAccount(accountId);

        return transactionRepository.findByAccountAccountIdOrderByEventDateAsc(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private @NonNull AccountEntity getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("Attempt to create transaction for non-existent accountId={}", accountId);
                    return new AccountNotFoundException(accountId);
                });
    }

    private BigDecimal resolveBalance(OperationType operationType, Long accountId, BigDecimal normalizedAmount) {
        if (operationType.isDebit()) {
            return normalizedAmount;
        }
        return dischargeService.applyCreditToDebits(accountId,normalizedAmount);
    }

    private TransactionEntity buildTransaction(AccountEntity account, TransactionDTORequest request,
                                               BigDecimal normalizedAmount, BigDecimal balance) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setOperationTypeId(request.getOperationTypeId());
        transaction.setAmount(normalizedAmount);
        transaction.setEventDate(LocalDateTime.now());
        transaction.setBalance(balance);
        return transaction;
    }

    private TransactionDTOResponse toResponse(TransactionEntity transaction) {
        return new TransactionDTOResponse(
                transaction.getId(),
                transaction.getAccount().getAccountId(),
                transaction.getOperationTypeId(),
                transaction.getAmount(),
                transaction.getBalance()
        );
    }


}
