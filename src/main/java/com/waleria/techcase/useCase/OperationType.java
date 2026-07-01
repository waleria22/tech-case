package com.waleria.techcase.useCase;

import com.waleria.techcase.web.exception.OperationTypeNotFoundException;

import java.math.BigDecimal;

public enum OperationType {

    NORMAL_PURCHASE(1L) {
        @Override
        public BigDecimal normalize(BigDecimal amount) {
            return amount.abs().negate();
        }
    },

    INSTALLMENT_PURCHASE (2L) {
        @Override
        public BigDecimal normalize(BigDecimal amount) {
            return amount.abs().negate();
        }
    },

    WITHDRAWAL(3L) {
        @Override
        public BigDecimal normalize(BigDecimal amount) {
            return amount.abs().negate();
        }
    },

    CREDIT_VOUCHER(4L) {
        @Override
        public BigDecimal normalize(BigDecimal amount) {
            return amount.abs();
        }
    };

    private final Long id;

    OperationType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static OperationType fromId(Long id) {
        for (OperationType operationType : values()) {
            if (operationType.getId().equals(id)) {
                return operationType;
            }
        }

        throw new OperationTypeNotFoundException(id);
    }

    public abstract BigDecimal normalize(BigDecimal amount);
}
