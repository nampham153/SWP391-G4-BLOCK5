package com.example.swp.service.impl;

import com.example.swp.dto.OrderRequest;
import com.example.swp.dto.StorageRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import com.example.swp.entity.StorageTransaction;
import com.example.swp.enums.TransactionType;
import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.OrderRepository;
import com.example.swp.repository.StorageRepository;
import com.example.swp.service.ActivityLogService;
import com.example.swp.service.OrderService;
import com.example.swp.service.StorageService;
import com.example.swp.service.StorageTransactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Component
public class OrderServiceimpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private StorageRepository storageReponsitory;
    @Autowired
    private StorageRequest storageRequest;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ActivityLogService activityLogService;
    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageTransactionService storageTransactionService;
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    @Override
    public boolean canCustomerFeedback(int customerId, int storageId) {
        return orderRepository.existsByCustomer_IdAndStorage_StorageidAndStatusIn(
                customerId,
                storageId,
                List.of("PAID")
        );
    }


    @Override
    public Optional<Order> getOrderById(int id) {return orderRepository.findById(id);}


    @Override
    public List<Order> findOrdersByCustomer(Customer customer) {
        return orderRepository.findByCustomer(customer);
    }

    @Override
    public Optional<Order> findOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order createOrder(OrderRequest orderRequest) {
        Customer customer = customerRepository.findById(orderRequest.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không có customer " + orderRequest.getCustomerId()));

        Storage storage = storageReponsitory.findById(orderRequest.getStorageId())
                .orElseThrow(() -> new RuntimeException("Không có storage " + orderRequest.getStorageId()));

        boolean available = isStorageAvailable(storage.getStorageid(),
                orderRequest.getStartDate(), orderRequest.getEndDate());

        if (!available) {
            throw new RuntimeException("Kho đã có người đặt trong khoảng thời gian này!");
        }

        long rentalDays = ChronoUnit.DAYS.between(orderRequest.getStartDate(), orderRequest.getEndDate());
        if (rentalDays <= 0) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
        }

        double dailyRate = storage.getPricePerDay();
        double totalAmount = rentalDays * dailyRate;

        Order order = new Order();
        order.setStartDate(orderRequest.getStartDate());
        order.setEndDate(orderRequest.getEndDate());
        order.setOrderDate(orderRequest.getOrderDate());
        rentalDays = ChronoUnit.DAYS.between(orderRequest.getStartDate(), orderRequest.getEndDate());
        // Tính tổng tiền thuê
         dailyRate =storage.getPricePerDay(); // hoặc giá cố định
         totalAmount = rentalDays * dailyRate;
        order.setTotalAmount(totalAmount);
        order.setStatus(orderRequest.getStatus().toUpperCase());
        order.setCustomer(customer);
        order.setStorage(storage);

        Order savedOrder = orderRepository.save(order);
        storageService.updateStatusBasedOnAvailability(storage.getStorageid(), order.getStartDate(), order.getEndDate());

        activityLogService.logActivity(
                "Tạo đơn hàng",
                "Khách hàng " + customer.getFullname() + " đã tạo đơn hàng #" + savedOrder.getId(),
                customer,
                savedOrder,
                null, null, null, null
        );
        // -----------------------------------------

        return savedOrder;



    }

    @Override
    public List<Order> findOrdersByStatus(String status) {
        return orderRepository.findByStatus(status.toUpperCase());
    }
    @Override
    public void deleteById(int id) {
        orderRepository.deleteById(id);
    }



    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    //Hàm tính total amount
    public BigDecimal calculateTotalAmount(LocalDate startDate, LocalDate endDate, BigDecimal pricePerDay) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        return pricePerDay.multiply(BigDecimal.valueOf(days));
    }



    @Override
    public double getTotalRevenueAll() {
        return orderRepository.findAll()
                .stream()
                .filter(order -> !"REJECTED".equalsIgnoreCase(order.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    @Override
    public double getRevenuePaid() {
        return orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    @Override
    public double getRevenueApproved() {
        return orderRepository.findAll()
                .stream()
                .filter(order -> "APPROVED".equalsIgnoreCase(order.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

//    //Hàm tính total amount
//    public BigDecimal calculateTotalAmount(LocalDate startDate, LocalDate endDate, BigDecimal pricePerDay) {
//        long days = ChronoUnit.DAYS.between(startDate, endDate);
//        if (days <= 0) {
//            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
//        }
//        return pricePerDay.multiply(BigDecimal.valueOf(days));
//
//
//    }

    @Transactional
    public void markOrderAsPaid(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Order với ID: " + orderId));

        long overlap = orderRepository.countOverlapOrders(
                order.getStorage().getStorageid(),
                order.getStartDate(),
                order.getEndDate()
        );

        if (overlap > 1) {
            throw new RuntimeException("Kho đã có người khác đặt trong khoảng thời gian này. Không thể thanh toán đơn hàng này.");
        }

        orderRepository.updateOrderStatusToPaid(orderId);
        Storage storage = order.getStorage();
        storage.setStatus(false); // Đang được thuê
        storageReponsitory.save(storage); // lưu lại thay đổi

        // Tạo StorageTransaction khi đơn hàng được đánh dấu là PAID
        StorageTransaction transaction = new StorageTransaction();
        transaction.setType(TransactionType.PAID);
        transaction.setTransactionDate(LocalDateTime.now()); // sửa thành LocalDateTime.now()
        transaction.setAmount(order.getTotalAmount());
        transaction.setStorage(order.getStorage());
        transaction.setCustomer(order.getCustomer());
        transaction.setOrder(order); // Liên kết đến order đầy đủ hơn

        storageTransactionService.save(transaction);

        // ---------- GHI LOG HOẠT ĐỘNG ----------
        activityLogService.logActivity(
                "Thanh toán đơn hàng",
                "Khách hàng " + order.getCustomer().getFullname() + " đã thanh toán đơn hàng #" + order.getId(),
                order.getCustomer(),
                order,
                transaction,
                null, null, null
        );
        storageService.updateStatusBasedOnAvailability(
                storage.getStorageid(),
                order.getStartDate(),
                order.getEndDate()
        );
    }

    // Trong OrderServiceImpl
    @Override
    public Map<String, Long> countOrdersByStatus() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().collect(
                Collectors.groupingBy(Order::getStatus, Collectors.counting())
        );
    }

    @Override
    public List<Order> getLast5orders() {
        return orderRepository.findTop5ByOrderByOrderDateDesc();
    }



    @Override
    public boolean isStorageAvailable(int storageId, LocalDate startDate, LocalDate endDate) {
        return orderRepository.countOverlapOrders(storageId, startDate, endDate) == 0;
    }

    @Override
    public long countOverlapOrdersByCustomer(int customerId, int storageId, LocalDate startDate, LocalDate endDate) {
        return orderRepository.countOverlapOrdersByCustomer(customerId, storageId, startDate, endDate);
    }

    @Override
    public Order createBookingOrder(Storage storage, Customer customer,
                                    LocalDate startDate, LocalDate endDate, double total) {
        boolean available = isStorageAvailable(storage.getStorageid(), startDate, endDate);
        if (!available) {
            throw new RuntimeException("Kho đã có người đặt trong khoảng thời gian này!");
        }
        Order order = new Order();
        order.setStorage(storage);
        order.setCustomer(customer);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        // Cần setRentalArea từ controller truyền vào!
        // order.setRentalArea(rentalArea);

        Order savedOrder = orderRepository.save(order);
        storageService.updateStatusBasedOnAvailability(storage.getStorageid(), startDate, endDate);
        // ----------- GHI LOG HOẠT ĐỘNG -----------
        activityLogService.logActivity(
                "Tạo đơn booking",
                "Khách hàng " + customer.getFullname() + " tạo đơn booking #" + savedOrder.getId(),
                customer,
                savedOrder,
                null, null, null, null
        );
        // -----------------------------------------

        return savedOrder;
    }


    @Override
    public double getTotalRentedArea(int storageId) {
        return 0;
    }
    @Transactional
    public void updateOrderStatusToPaid(int orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (!"PAID".equals(order.getStatus())) {
                order.setStatus("PAID");

                // ✅ Cộng điểm cho khách hàng
                Customer customer = order.getCustomer();
                if (customer != null) {
                    customer.setPoints(customer.getPoints() + 5); // cộng 5 điểm
                    customerRepository.save(customer); // lưu lại customer
                }

                orderRepository.updateOrderStatusToPaid(orderId);
            }
        }
    }

    @Override
    public double getRemainArea(int storageId, LocalDate startDate, LocalDate endDate) {
        Optional<Storage> storageOpt = storageReponsitory.findById(storageId);
        if (storageOpt.isEmpty()) return 0.0;
        double totalArea = storageOpt.get().getArea();
        double maxUsed = 0;

        for (LocalDate d = startDate; !d.isAfter(endDate.minusDays(1)); d = d.plusDays(1)) {
            // Tính tổng diện tích đã bị đặt cho ngày d (các order trùng ngày d, trạng thái còn hiệu lực)
            double used = orderRepository.sumRentedAreaForStorageOnDate(storageId, d);
            if (used > maxUsed) maxUsed = used;
        }
        return Math.max(0, totalArea - maxUsed);
    }
    public Optional<Order> findOrderByCustomerAndStorage(int customerId, int storageId) {
        return orderRepository.findByCustomer_IdAndStorage_Storageid(customerId, storageId);
    }

//    @Override
//    public List<Order> getLast5Orders() {
//        return List.of();
//    }


}
