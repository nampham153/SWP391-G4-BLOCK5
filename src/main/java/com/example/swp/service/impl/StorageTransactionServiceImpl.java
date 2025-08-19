package com.example.swp.service.impl;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import com.example.swp.entity.StorageTransaction;
import com.example.swp.enums.TransactionType;
import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.OrderRepository;
import com.example.swp.repository.StorageRepository;
import com.example.swp.repository.StorageTransactionRepository;
import com.example.swp.service.ActivityLogService;
import com.example.swp.service.StorageTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StorageTransactionServiceImpl implements StorageTransactionService {

    @Autowired
    private StorageTransactionRepository storageTransactionRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ActivityLogService activityLogService;

    @Override
    public List<StorageTransaction> getAllStorageTransactions() {
        return storageTransactionRepository.findAll();
    }

    @Override
    public Optional<StorageTransaction> getStorageTransactionById(int id) {
        return storageTransactionRepository.findById(id);
    }

    @Override
    public List<StorageTransaction> findByCustomerId(int customerId) {
        return storageTransactionRepository.findByCustomer_Id(customerId);
    }

    @Override
    public List<StorageTransaction> findByOrderId(int orderId) {
        return storageTransactionRepository.findByOrder_Id(orderId);
    }

    @Override
    public StorageTransaction save(StorageTransaction transaction) {
        // Validate type hợp lệ
        if (transaction.getType() != TransactionType.PAID && transaction.getType() != TransactionType.REFUND && transaction.getType() != TransactionType.REQUESTED) {
            throw new IllegalArgumentException("Chỉ cho phép PAID hoặc REFUND.");
        }
        return storageTransactionRepository.save(transaction);
    }

    @Override
    public StorageTransaction createTransaction(TransactionType type, double amount, int storageId, int customerId, int orderId) {
        if (type != TransactionType.PAID && type != TransactionType.REFUND) {
            throw new IllegalArgumentException("Chỉ cho phép PAID hoặc REFUND.");
        }

        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        StorageTransaction transaction = new StorageTransaction();
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAmount(amount);
        transaction.setStorage(storage);
        transaction.setCustomer(customer);
        transaction.setOrder(order);

        StorageTransaction savedTransaction = storageTransactionRepository.save(transaction);

        // Ghi log transaction
        String action = (type == TransactionType.PAID) ? "Thanh toán" : "Hoàn tiền";
        String description = "Khách hàng " + customer.getFullname()
                + " " + (type == TransactionType.PAID ? "thanh toán" : "được hoàn tiền")
                + " số tiền: " + String.format("%,.0f", amount) + "đ"
                + " cho kho " + storage.getStoragename()
                + " - Đơn hàng #" + order.getId()
                + " - Mã giao dịch: " + savedTransaction.getId();

        activityLogService.logActivity(
                action,
                description,
                customer,
                order, savedTransaction, null, null, null
        );

        return savedTransaction;
    }
    @Override
    public List<StorageTransaction> findByType(TransactionType type) {
        return storageTransactionRepository.findByType(type);
    }
    @Override
    public List<StorageTransaction> getFilteredTransactions(TransactionType type, String customerKeyword, String storageKeyword) {
        return storageTransactionRepository.findAll().stream()
                .filter(t -> type == null || t.getType() == type)
                .filter(t -> customerKeyword == null || t.getCustomer().getFullname().toLowerCase().contains(customerKeyword.toLowerCase()))
                .filter(t -> storageKeyword == null || t.getStorage().getStoragename().toLowerCase().contains(storageKeyword.toLowerCase()))
                .toList();
    }



}
