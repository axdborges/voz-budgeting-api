package com.axdborges.voz.budgeting.domain;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(TransactionId id) {
        super("Transação não encontrada: " + id.value());
    }
}
