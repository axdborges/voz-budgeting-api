package com.axdborges.voz.budgeting.domain;

import java.util.UUID;

public record TransactionId(UUID value) {

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId of(String value) {
        try {
            return new TransactionId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("id inválido, esperado um UUID: " + value);
        }
    }
}
