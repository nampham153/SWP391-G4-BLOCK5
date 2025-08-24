package com.example.swp.repository;

import com.example.swp.entity.StorageInventoryTransaction;
import com.example.swp.enums.InventoryTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageInventoryTransactionRepository extends JpaRepository<StorageInventoryTransaction, Integer> {
    List<StorageInventoryTransaction> findByCustomerId(Integer customerId);

    List<StorageInventoryTransaction> findByStatus(InventoryTransactionStatus status);
}