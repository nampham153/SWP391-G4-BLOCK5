package com.example.swp.service.impl;

import com.example.swp.entity.EContract;
import com.example.swp.entity.Order;
import com.example.swp.enums.EContractStatus;
import com.example.swp.repository.EContractRepository;
import com.example.swp.service.EContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EContractServiceImpl implements EContractService {
    
    @Autowired
    private EContractRepository eContractRepository;
    
    @Override
    public EContract createContract(Order order) {
        EContract contract = new EContract();
        contract.setContractCode("HD-" + order.getId());
        contract.setOrder(order);
        contract.setStatus(EContractStatus.PENDING);
        contract.setPricePerDay(order.getStorage().getPricePerDay());
        contract.setRentalArea(order.getRentalArea());
        contract.setTotalAmount(order.getTotalAmount());
        contract.setStorageName(order.getStorage().getStoragename());
        
        return eContractRepository.save(contract);
    }
    
    @Override
    public Optional<EContract> findByOrder(Order order) {
        return eContractRepository.findByOrder(order);
    }
    
    @Override
    public Optional<EContract> findByOrderId(Integer orderId) {
        return eContractRepository.findByOrderId(orderId);
    }
    
    @Override
    public Optional<EContract> findById(Long id) {
        return eContractRepository.findById(id);
    }
    
    @Override
    public EContract signContract(Long contractId) {
        Optional<EContract> contractOpt = eContractRepository.findById(contractId);
        if (contractOpt.isPresent()) {
            EContract contract = contractOpt.get();
            contract.setStatus(EContractStatus.SIGNED);
            contract.setSignedAt(LocalDateTime.now());
            return eContractRepository.save(contract);
        }
        throw new RuntimeException("Contract not found with ID: " + contractId);
    }
    
    @Override
    public List<EContract> findByCustomerId(Integer customerId) {
        return eContractRepository.findByOrderCustomerId(customerId);
    }
    
    @Override
    public boolean areAllContractsSignedForCustomer(Integer customerId) {
        List<EContract> customerContracts = findByCustomerId(customerId);
        if (customerContracts.isEmpty()) {
            return false;
        }
        
        // Chỉ kiểm tra hợp đồng từ các đơn hàng chưa bị hủy
        List<EContract> activeContracts = customerContracts.stream()
                .filter(contract -> !"CANCELLED".equals(contract.getOrder().getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        if (activeContracts.isEmpty()) {
            return false;
        }
        
        return activeContracts.stream()
                .allMatch(contract -> contract.getStatus() == EContractStatus.SIGNED);
    }
    
    @Override
    public EContract save(EContract contract) {
        return eContractRepository.save(contract);
    }
    
    @Override
    public long countSignedContractsByCustomer(Integer customerId) {
        return eContractRepository.countByOrderCustomerIdAndStatus(customerId, EContractStatus.SIGNED);
    }
}
