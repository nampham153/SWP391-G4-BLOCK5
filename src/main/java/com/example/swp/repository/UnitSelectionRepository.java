package com.example.swp.repository;

import com.example.swp.entity.UnitSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UnitSelectionRepository extends JpaRepository<UnitSelection, Long> {

    @Query("""
        SELECT u.unitIndex
        FROM UnitSelection u
        JOIN u.order o
        WHERE u.storage.storageid = :storageId
          AND o.status IN ('PENDING','CONFIRMED','ACTIVE','PAID')
          AND u.startDate < :endDate AND u.endDate > :startDate
    """)
    List<Integer> findBookedUnitIndicesForOverlap(
            @Param("storageId") int storageId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT u.unitIndex
        FROM UnitSelection u
        WHERE u.order.id = :orderId
        ORDER BY u.unitIndex
    """)
    List<Integer> findUnitIndicesByOrderId(@Param("orderId") int orderId);
}
