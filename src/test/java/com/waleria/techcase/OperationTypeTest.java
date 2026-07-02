package com.waleria.techcase;

import com.waleria.techcase.useCase.OperationType;
import com.waleria.techcase.web.exception.OperationTypeNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationTypeTest {

    @ParameterizedTest(name = "should normalize {1} to {2} when operationTypeId={0}")
    @CsvSource({
            "1, 50.00, -50.00",   // NORMAL_PURCHASE
            "2, 30.00, -30.00",   // INSTALLMENT_PURCHASE
            "3, 100.00, -100.00", // WITHDRAWAL
            "4, 60.00, 60.00"     // CREDIT_VOUCHER
    })
    void shouldNormalizeAmountCorrectlyAccordingToOperationType(Long operationTypeId, BigDecimal input, BigDecimal expected) {
        OperationType operationType = OperationType.fromId(operationTypeId);

        BigDecimal result = operationType.normalize(input);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @ParameterizedTest(name = "operationTypeId={0} should correctly normalize negative input value")
    @CsvSource({
            "1, -50.00, -50.00",  // já negativo, deve continuar negativo
            "4, -60.00, 60.00"    // negativo indevido em crédito deve virar positivo
    })
    void shouldNormalizeCorrectlyEvenWhenAmountHasWrongSign(Long operationTypeId, BigDecimal input, BigDecimal expected) {
        OperationType operationType = OperationType.fromId(operationTypeId);

        BigDecimal result = operationType.normalize(input);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void shouldThrowExceptionWhenOperationTypeIdDoesNotExist() {
        Long id = 99L;

        assertThatThrownBy(() -> OperationType.fromId(id))
                .isInstanceOf(OperationTypeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldThrowExceptionWhenOperationTypeIdIsNull() {
        assertThatThrownBy(() -> OperationType.fromId(null))
                .isInstanceOf(OperationTypeNotFoundException.class);
    }
}
