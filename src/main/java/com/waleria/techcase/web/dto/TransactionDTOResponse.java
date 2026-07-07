package com.waleria.techcase.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDTOResponse {
    Long transactionId;
    Long accountId;
    Long operationTypeId;
    BigDecimal amount;
    BigDecimal balance;

    public TransactionDTOResponse(Long id, Long accountId, Long operationTypeId, BigDecimal amount, BigDecimal balance) {
        this.transactionId = id;
                this.accountId = accountId;
                this.operationTypeId = operationTypeId;
                this.amount = amount;
                this.balance = balance;
    }

}
