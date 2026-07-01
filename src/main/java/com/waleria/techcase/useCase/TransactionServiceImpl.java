package com.waleria.techcase.useCase;


import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.repository.TransactionEntity;
import com.waleria.techcase.repository.TransactionRepository;
import com.waleria.techcase.service.TransactionService;
import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    @Override
    public TransactionDTOResponse createTransaction(TransactionDTORequest request) {

        if (!accountRepository.existsById(request.getAccountId())) {
            throw new AccountNotFoundException(request.getAccountId());
        }
        OperationType operationType = OperationType.fromId(request.getOperationTypeId());
        BigDecimal normalizedAmount = operationType.normalize(request.getAmount());

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAccountId(request.getAccountId());
        transactionEntity.setOperationTypeId(request.getOperationTypeId());
        transactionEntity.setAmount(normalizedAmount);
        transactionEntity.setEventDate(LocalDateTime.now());

         TransactionEntity transactionSave = transactionRepository.save(transactionEntity);

        return new TransactionDTOResponse(
                transactionSave.getId(),
                transactionSave.getAccountId(),
                transactionSave.getOperationTypeId(),
                transactionSave.getAmount()
        );
    }
}
