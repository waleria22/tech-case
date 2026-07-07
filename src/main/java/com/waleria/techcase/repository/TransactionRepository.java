package com.waleria.techcase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity,Long> {
    List<TransactionEntity> findByAccountAccountIdAndBalanceLessThanOrderByEventDateAsc(
            Long accountId,
            BigDecimal amount);

    List<TransactionEntity> findByAccountAccountIdOrderByEventDateAsc(Long accountId);

}
