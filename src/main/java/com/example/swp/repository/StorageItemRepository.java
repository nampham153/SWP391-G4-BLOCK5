package com.example.swp.repository;

import com.example.swp.entity.StorageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface StorageItemRepository extends JpaRepository<StorageItem, Integer> {
    @Query("SELECT i FROM StorageItem i WHERE i.storageId = :storageId AND i.zoneId = :zoneId")
    List<StorageItem> findByStorageAndZone(Integer storageId, Integer zoneId);

    @Query("SELECT i FROM StorageItem i WHERE i.orderId = :orderId AND i.itemName = :itemName")
    Optional<StorageItem> findByOrderAndItemName(Integer orderId, String itemName);
}