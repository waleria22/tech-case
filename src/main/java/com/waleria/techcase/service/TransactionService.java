package com.waleria.techcase.service;

import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;

import java.util.List;

public interface TransactionService {

    TransactionDTOResponse createTransaction(TransactionDTORequest request);

    List<TransactionDTOResponse> listTransactionsByAccount(Long accountId);
}
