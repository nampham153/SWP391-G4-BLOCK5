package com.example.swp.repository;

import com.example.swp.entity.EContract;
import com.example.swp.entity.Order;
import com.example.swp.enums.EContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EContractRepository extends JpaRepository<EContract, Long> {
    
    Optional<EContract> findByOrder(Order order);
    
    Optional<EContract> findByOrderId(Integer orderId);
    
    List<EContract> findByStatus(EContractStatus status);
    
    Optional<EContract> findByContractCode(String contractCode);
    
    List<EContract> findByOrderCustomerId(Integer customerId);
    
    long countByOrderCustomerIdAndStatus(Integer customerId, EContractStatus status);
}
