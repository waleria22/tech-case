package com.waleria.techcase.web.exception;

public class OperationTypeNotFoundException extends RuntimeException {
    public OperationTypeNotFoundException(Long operationTypeId) {
        super("Operation type not found: " + operationTypeId);
    }
}
