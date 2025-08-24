package com.example.swp.service;

import com.example.swp.dto.InventoryRequestCreateDTO;
import com.example.swp.dto.InventoryRequestResponseDTO;
import com.example.swp.dto.StorageItemViewDTO;

import java.util.List;

public interface InventoryRequestService {
    InventoryRequestResponseDTO createRequest(Integer customerId, InventoryRequestCreateDTO dto);

    List<InventoryRequestResponseDTO> getRequestsByCustomer(Integer customerId);

    List<StorageItemViewDTO> getStorageItems(Integer customerId);

    List<InventoryRequestResponseDTO> getPendingRequests();

    InventoryRequestResponseDTO approveRequest(Integer requestId, Integer staffId);

    InventoryRequestResponseDTO rejectRequest(Integer requestId, String reason, Integer staffId);
}
