package com.waleria.techcase.useCase;


import com.waleria.techcase.repository.AccountEntity;
import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.service.TransactionService;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    @Override
    public TransactionDTOResponse createTransaction(TransactionDTORequest request) {

        AccountEntity account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Attempt to create transaction for non-existent accountId={}", request.getAccountId());
                    return new AccountNotFoundException(request.getAccountId());
                });
        OperationType operationType = OperationType.fromId(request.getOperationTypeId());
        BigDecimal normalizedAmount = operationType.normalize(request.getAmount());

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAccount(account);
        transactionEntity.setOperationTypeId(request.getOperationTypeId());
        transactionEntity.setAmount(normalizedAmount);
        transactionEntity.setEventDate(LocalDateTime.now());

         TransactionEntity transactionSave = transactionRepository.save(transactionEntity);

        return new TransactionDTOResponse(
                transactionSave.getId(),
                transactionSave.getAccount().getAccountId(),
                transactionSave.getOperationTypeId(),
                transactionSave.getAmount()
        );
    }
}
