package com.waleria.techcase.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountDTORequest {

    @NotBlank
    @JsonProperty("document_number")
   String documentNumber;

    public AccountDTORequest(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}
