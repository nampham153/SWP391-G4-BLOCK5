package com.example.swp.repository;

import com.example.swp.entity.StorageTransaction;
import com.example.swp.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageTransactionRepository extends JpaRepository<StorageTransaction, Integer> {
    List<StorageTransaction> findByCustomer_Id(Integer customerId);
    List<StorageTransaction> findByOrder_Id(int orderId);
    List<StorageTransaction> findByType(TransactionType type);

}