package com.waleria.techcase.service;


import java.math.BigDecimal;

public interface DischargeService {


    BigDecimal applyCreditToTransactionsDebit(Long accountId, BigDecimal creditAmount);
}
