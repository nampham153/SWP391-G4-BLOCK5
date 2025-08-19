package com.example.swp.repository;

import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.swp.entity.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByStatus(String status);

    List<Order> findByCustomer(Customer customer);

    @Modifying
    @Query("UPDATE Order o SET o.status = 'PAID' WHERE o.id = :orderId")
    void updateOrderStatusToPaid(@Param("orderId") int orderId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'PAID'")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.storage.storageid = :storageId " +
            "AND o.status IN ('PAID', 'APPROVED', 'ACTIVE') " +
            "AND o.startDate < :endDate AND o.endDate > :startDate")
    long countOverlapOrders(@Param("storageId") int storageId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(SUM(o.rentalArea), 0.0) FROM Order o " +
            "WHERE o.storage.storageid = :storageId " +
            "AND o.status IN ('PENDING','CONFIRMED','ACTIVE', 'PAID') " +
            "AND :targetDate BETWEEN o.startDate AND o.endDate")
    Double sumRentedAreaForStorageOnDate(@Param("storageId") int storageId, @Param("targetDate") LocalDate targetDate);



    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.customer.id = :customerId
          AND o.storage.storageid = :storageId
          AND o.status IN ('PENDING','CONFIRMED','ACTIVE')
          AND o.startDate <= :endDate
          AND o.endDate >= :startDate
    """)
    long countOverlapOrdersByCustomer(
            @Param("customerId") int customerId,
            @Param("storageId") int storageId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("SELECT COALESCE(SUM(o.rentalArea),0) FROM Order o WHERE o.storage.storageid = :storageId AND (o.status = 'CONFIRMED' OR o.status = 'PAID') AND o.endDate >= :now")
    Double getTotalRentedArea(@Param("storageId") int storageId, @Param("now") LocalDate now);


    Optional<Order> findById(int id);

    List<Order> findTop5ByOrderByOrderDateDesc();
    Optional<Order> findByCustomer_IdAndStorage_Storageid(int customerId, int storageId);
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.customer.id = :customerId AND o.storage.storageid = :storageId AND o.status IN :statuses")
    boolean existsByCustomer_IdAndStorage_StorageidAndStatusIn(
            @Param("customerId") int customerId,
            @Param("storageId") int storageId,
            @Param("statuses") List<String> statuses
    );



    @Query("SELECT o FROM Order o WHERE o.status = 'PAID' AND o.endDate <= :threshold AND o.endDate >= :today")
    List<Order> findPaidRenewalOrders(LocalDate today, LocalDate threshold);

}


