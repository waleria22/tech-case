package com.waleria.techcase.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDTORequest {

    @NotNull
    @JsonProperty("account_id")
    Long accountId;

    @NotNull
    @JsonProperty("operation_type_id")
    Long operationTypeId;

    @NotNull
    @JsonProperty("amount")
    BigDecimal amount;
}
