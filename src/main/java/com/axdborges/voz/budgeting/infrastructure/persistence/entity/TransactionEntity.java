package com.axdborges.voz.budgeting.infrastructure.persistence.entity;

import com.axdborges.voz.budgeting.domain.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private String id;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private BigDecimal amount;

    private LocalDateTime occurredAt;

    private LocalDateTime updatedAt;

    protected TransactionEntity() {
    }

    public TransactionEntity(String id, String description, Category category, BigDecimal amount,
                              LocalDateTime occurredAt, LocalDateTime updatedAt) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
