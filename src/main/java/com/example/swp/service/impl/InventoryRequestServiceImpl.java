package com.example.swp.service.impl;

import com.example.swp.dto.*;
import com.example.swp.entity.*;
import com.example.swp.enums.*;
import com.example.swp.repository.*;
import com.example.swp.service.InventoryRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryRequestServiceImpl implements InventoryRequestService {
    private final OrderRepository orderRepository;
    private final StorageRepository storageRepository;
    private final ZoneRepository zoneRepository;
    private final StorageItemRepository storageItemRepository;
    private final StorageInventoryTransactionRepository transactionRepository;

    @Override
    public InventoryRequestResponseDTO createRequest(Integer customerId, InventoryRequestCreateDTO dto) {
        Order order = orderRepository.findByIdAndCustomerId(dto.getOrderId(), customerId)
                .orElseThrow(() -> new RuntimeException("Order không hợp lệ"));

        if (!"PAID".equalsIgnoreCase(order.getStatus())
                || (order.getEndDate() != null && order.getEndDate().isBefore(LocalDate.now()))) {
            throw new RuntimeException("Order đã hết hạn hoặc chưa thanh toán");
        }

        StorageInventoryTransaction tx = StorageInventoryTransaction.builder()
                .transactionDate(OffsetDateTime.now())
                .transactionType(dto.getTransactionType())
                .status(InventoryTransactionStatus.PENDING)
                .customerId(customerId)
                .storageId(order.getId())
                .orderId(order.getId())
                .zoneId(order.getId())
                .quantity(dto.getQuantity())
                .note(dto.getItemName() + "|" + dto.getVolumePerUnit())
                .build();
        transactionRepository.save(tx);

        return mapToDTO(tx, dto.getItemName(), dto.getVolumePerUnit(),
                storageRepository.findById(order.getId()).map(Storage::getStoragename).orElse(""),
                zoneRepository.findById(order.getId()).map(Zone::getName).orElse(""));
    }

    @Override
    public List<InventoryRequestResponseDTO> getRequestsByCustomer(Integer customerId) {
        return transactionRepository.findByCustomerId(customerId).stream()
                .map(tx -> mapToDTO(tx, extractItemName(tx.getNote()), extractVolume(tx.getNote()), "", ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<StorageItemViewDTO> getStorageItems(Integer customerId) {
        List<Order> orders = orderRepository.findActivePaidOrders(customerId, LocalDate.now());
        return orders.stream()
                .flatMap(o -> storageItemRepository.findByStorageAndZone(o.getId(), o.getId()).stream())
                .map(i -> new StorageItemViewDTO(i.getId(),
                        storageRepository.findById(i.getStorageId()).map(Storage::getStoragename).orElse(""),
                        zoneRepository.findById(i.getZoneId()).map(Zone::getName).orElse(""),
                        i.getItemName(), i.getVolumePerUnit(), i.getQuantity(), i.getDateStored()))
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryRequestResponseDTO> getPendingRequests() {
        return transactionRepository.findByStatus(InventoryTransactionStatus.PENDING).stream()
                .map(tx -> mapToDTO(tx, extractItemName(tx.getNote()), extractVolume(tx.getNote()), "", ""))
                .collect(Collectors.toList());
    }

    @Override
    public InventoryRequestResponseDTO approveRequest(Integer requestId, Integer staffId) {
        StorageInventoryTransaction tx = transactionRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (tx.getStatus() != InventoryTransactionStatus.PENDING)
            throw new RuntimeException("Request already processed");

        String itemName = extractItemName(tx.getNote());
        Double volume = extractVolume(tx.getNote());

        if (tx.getTransactionType() == InventoryTransactionType.INBOUND) {
            storageItemRepository.findByOrderAndItemName(tx.getOrderId(), itemName)
                    .ifPresentOrElse(item -> {
                        item.setQuantity(item.getQuantity() + tx.getQuantity());
                        storageItemRepository.save(item);
                    }, () -> storageItemRepository.save(StorageItem.builder()
                            .storageId(tx.getStorageId())
                            .zoneId(tx.getZoneId())
                            .orderId(tx.getOrderId())
                            .itemName(itemName)
                            .volumePerUnit(volume)
                            .quantity(tx.getQuantity())
                            .dateStored(LocalDate.now())
                            .build()));
        } else { // OUTBOUND
            StorageItem item = storageItemRepository.findByOrderAndItemName(tx.getOrderId(), itemName)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            if (item.getQuantity() < tx.getQuantity()) {
                return rejectRequest(requestId, "Không đủ hàng để xuất", staffId);
            }
            item.setQuantity(item.getQuantity() - tx.getQuantity());
            storageItemRepository.save(item);
        }

        tx.setStatus(InventoryTransactionStatus.APPROVED);
        transactionRepository.save(tx);

        return mapToDTO(tx, itemName, volume, "", "");
    }

    @Override
    public InventoryRequestResponseDTO rejectRequest(Integer requestId, String reason, Integer staffId) {
        StorageInventoryTransaction tx = transactionRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (tx.getStatus() != InventoryTransactionStatus.PENDING)
            throw new RuntimeException("Request already processed");

        tx.setStatus(InventoryTransactionStatus.REJECTED);
        tx.setRejectionReason(reason);
        transactionRepository.save(tx);

        return mapToDTO(tx, extractItemName(tx.getNote()), extractVolume(tx.getNote()), "", "");
    }

    private InventoryRequestResponseDTO mapToDTO(StorageInventoryTransaction tx,
                                                 String itemName, Double volume,
                                                 String storageName, String zoneName) {
        return InventoryRequestResponseDTO.builder()
                .id(tx.getId())
                .transactionDate(tx.getTransactionDate())
                .transactionType(tx.getTransactionType())
                .status(tx.getStatus())
                .orderId(tx.getOrderId())
                .storageId(tx.getStorageId())
                .zoneId(tx.getZoneId())
                .quantity(tx.getQuantity())
                .itemName(itemName)
                .volumePerUnit(volume)
                .note(tx.getNote())
                .rejectionReason(tx.getRejectionReason())
                .storageName(storageName)
                .zoneName(zoneName)
                .build();
    }

    private String extractItemName(String note) {
        return note != null && note.contains("|") ? note.split("\\|")[0] : "";
    }

    private Double extractVolume(String note) {
        try {
            return note != null && note.contains("|") ? Double.parseDouble(note.split("\\|")[1]) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
