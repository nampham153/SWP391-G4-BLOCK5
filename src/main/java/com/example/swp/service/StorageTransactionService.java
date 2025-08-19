package com.example.swp.service;

import com.example.swp.entity.StorageTransaction;
import com.example.swp.enums.TransactionType;

import java.util.List;
import java.util.Optional;

public interface StorageTransactionService {
    List<StorageTransaction> getAllStorageTransactions();
    Optional<StorageTransaction> getStorageTransactionById(int id);

    // Lấy tất cả transaction của một khách hàng
    List<StorageTransaction> findByCustomerId(int customerId);

    // Lấy tất cả transaction theo order (nếu có nối Order)
    List<StorageTransaction> findByOrderId(int orderId);

    // Tạo transaction mới (PAID/REFUND)
    StorageTransaction createTransaction(TransactionType type, double amount, int storageId, int customerId, int orderId);

    StorageTransaction save(StorageTransaction transaction);
    List<StorageTransaction> findByType(TransactionType type);
     List<StorageTransaction> getFilteredTransactions(TransactionType type, String customerName, String storageName);


}
