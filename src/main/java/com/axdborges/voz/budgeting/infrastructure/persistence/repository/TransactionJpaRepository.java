package com.axdborges.voz.budgeting.infrastructure.persistence.repository;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, String> {

    List<TransactionEntity> findByCategory(Category category);
}
