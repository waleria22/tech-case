package com.waleria.techcase.service;


import java.math.BigDecimal;

public interface DischargeService {


    BigDecimal applyCreditToDebits(Long accountId, BigDecimal creditAmount);
}
