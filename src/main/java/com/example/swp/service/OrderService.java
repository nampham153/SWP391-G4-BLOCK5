package com.example.swp.service;

import com.example.swp.entity.Customer;
import com.example.swp.dto.OrderRequest;
import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface OrderService {
    List<Order> getAllOrders();

    Optional<Order> findOrderById(Integer id);

    List<Order> findOrdersByStatus(String status);

    Optional<Order> getOrderById(int id);

    Order createOrder(OrderRequest orderRequest);

    List<Order> findOrdersByCustomer(Customer customer);

    long countOverlapOrdersByCustomer(int customerId, int storageId, LocalDate startDate, LocalDate endDate);

    Order save(Order order);

    double getTotalRevenueAll(); // Tổng tiền các đơn trừ REJECTED

    double getRevenuePaid();     // Tiền khách đã trả (PAID)

    double getRevenueApproved(); // Tiền khách còn nợ (APPROVED)

    Map<String, Long> countOrdersByStatus();

    void deleteById(int id);

    boolean isStorageAvailable(int storageId, LocalDate startDate, LocalDate endDate);

    Order createBookingOrder(Storage storage, Customer customer, LocalDate startDate, LocalDate endDate, double total);

    double getTotalRentedArea(int storageId);

    double getRemainArea(int storageId, LocalDate startDate, LocalDate endDate);


    List<Order> getLast5orders();
    Optional<Order> findOrderByCustomerAndStorage(int customerId, int storageId);

    boolean canCustomerFeedback(int customerId, int storageId);
    void markOrderAsPaid(int orderId);

}
