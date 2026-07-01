package com.waleria.techcase.service;

import com.waleria.techcase.web.dto.TransactionDTORequest;
import com.waleria.techcase.web.dto.TransactionDTOResponse;

public interface TransactionService {

    TransactionDTOResponse createTransaction(TransactionDTORequest request);
}
