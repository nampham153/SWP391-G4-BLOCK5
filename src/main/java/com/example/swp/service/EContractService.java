package com.example.swp.service;

import com.example.swp.entity.EContract;
import com.example.swp.entity.Order;
import com.example.swp.enums.EContractStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EContractService {
    
    EContract createContract(Order order);
    
    Optional<EContract> findByOrder(Order order);
    
    Optional<EContract> findByOrderId(Integer orderId);
    
    Optional<EContract> findById(Long id);
    
    EContract signContract(Long contractId);
    
    List<EContract> findByCustomerId(Integer customerId);
    
    boolean areAllContractsSignedForCustomer(Integer customerId);
    
    EContract save(EContract contract);
    
    long countSignedContractsByCustomer(Integer customerId);
    
    List<EContract> findAll();
    
    List<EContract> findByStatus(EContractStatus status);
}
