package com.example.swp.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.swp.dto.OrderRequest;
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

@Component
public class OrderServiceimpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private StorageRepository storageReponsitory;
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
        // Tính tổng tiền thuê
        dailyRate = storage.getPricePerDay(); // hoặc giá cố định
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
        return orderRepository.findAll().stream()
                .filter(o -> {
                    String s = o.getStatus();
                    return s != null && (
                            "PAID".equalsIgnoreCase(s) ||
                                    "APPROVED".equalsIgnoreCase(s)
                    );
                })
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
//
//
//        }
//
//
//    }

    @Transactional
    @Override
    public void markOrderAsPaid(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Order với ID: " + orderId));

        // Idempotent: if already PAID, do nothing
        if ("PAID".equalsIgnoreCase(order.getStatus())) return;

        if (!"APPROVED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Trạng thái không hợp lệ để thanh toán: " + order.getStatus());
        }

        // Flip status
        order.setStatus("PAID");
        orderRepository.save(order);

        // Award points ONCE
        Customer customer = order.getCustomer();
        if (customer != null) {
            customer.setPoints(customer.getPoints() + 5);
            customerRepository.save(customer);
        }

        // Record transaction
        StorageTransaction tx = new StorageTransaction();
        tx.setType(TransactionType.PAID);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setAmount(order.getTotalAmount());
        tx.setStorage(order.getStorage());
        tx.setCustomer(order.getCustomer());
        tx.setOrder(order);
        storageTransactionService.save(tx);

        // Log
        activityLogService.logActivity(
                "Thanh toán đơn hàng",
                "Khách hàng " + order.getCustomer().getFullname() + " đã thanh toán đơn hàng #" + order.getId(),
                order.getCustomer(), order, tx, null, null, null
        );

        // Optional: refresh computed status (doesn't change total area)
        storageService.updateStatusBasedOnAvailability(
                order.getStorage().getStorageid(),
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
        Double total = orderRepository.getTotalRentedArea(storageId, LocalDate.now());
        return total == null ? 0.0 : total;
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
    @Override
    public List<Order> findExpiredOrdersByCustomer(int customerId) {
        return orderRepository.findExpiredOrdersByCustomer(customerId, LocalDate.now());
    }
    @Override
    public Optional<Order> findOrderByCustomerAndStorage(int customerId, int storageId) {
        return orderRepository.findByCustomer_IdAndStorage_Storageid(customerId, storageId);
    }

    @Override
    public List<Order> findActiveOrdersByCustomerAndStorage(int customerId, int storageId) {
        return orderRepository.findActiveOrdersByCustomerAndStorage(customerId, storageId);
    }

    @Override
    @Transactional
    public void cancelExistingOrdersForCustomerAndStorage(int customerId, int storageId, String reason) {
        List<Order> activeOrders = findActiveOrdersByCustomerAndStorage(customerId, storageId);
        for (Order order : activeOrders) {
            order.setStatus("CANCELLED");
            order.setCancelReason(reason);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Integer> findBookedZoneIds(int storageId, LocalDate startDate, LocalDate endDate) {
        System.out.println("[DEBUG] findBookedZoneIds called with:");
        System.out.println("  storageId: " + storageId);
        System.out.println("  startDate: " + startDate);
        System.out.println("  endDate: " + endDate);
        
        // Lấy danh sách zone đã được đặt và thanh toán (PAID hoặc CONFIRMED) theo tháng
        List<Order> allOrders = orderRepository.findAll();
        System.out.println("[DEBUG] Total orders in database: " + allOrders.size());
        
        List<Order> bookedOrders = allOrders.stream()
            .filter(order -> order.getStorage() != null && order.getStorage().getStorageid() == storageId)
            .filter(order -> "PAID".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus()))
            .filter(order -> order.getStartDate() != null && order.getEndDate() != null)
            .filter(order -> {
                boolean hasOverlap = hasMonthOverlap(order.getStartDate(), order.getEndDate(), startDate, endDate);
                System.out.println("[DEBUG] Order " + order.getId() + " (status: " + order.getStatus() + 
                    ", dates: " + order.getStartDate() + " to " + order.getEndDate() + 
                    ", selectedZoneIds: " + order.getSelectedZoneIds() + ") -> hasOverlap: " + hasOverlap);
                return hasOverlap;
            })
            .collect(Collectors.toList());
            
        System.out.println("[DEBUG] Found " + bookedOrders.size() + " booked orders with month overlap");
        
        // Trích xuất tất cả zone IDs từ cả selectedZoneIds và zone đơn lẻ
        Set<Integer> allZoneIds = new HashSet<>();
        
        for (Order order : bookedOrders) {
            // Ưu tiên lấy từ selectedZoneIds trước
            String selectedZoneIds = order.getSelectedZoneIds();
            if (selectedZoneIds != null && !selectedZoneIds.trim().isEmpty()) {
                try {
                    Arrays.stream(selectedZoneIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .forEach(allZoneIds::add);
                } catch (Exception e) {
                    // Nếu lỗi parse selectedZoneIds, fallback về zone đơn lẻ
                    if (order.getZone() != null) {
                        allZoneIds.add(order.getZone().getId());
                    }
                }
            } else if (order.getZone() != null) {
                // Fallback về zone đơn lẻ nếu không có selectedZoneIds
                allZoneIds.add(order.getZone().getId());
            }
        }
        
        System.out.println("[DEBUG] Final result - booked zone IDs: " + allZoneIds);
        return new ArrayList<>(allZoneIds);
    }
    
    /**
     * Kiểm tra overlap theo ngày (day-level):
     * Có overlap khi: !(orderEnd < queryStart || orderStart > queryEnd)
     * Điều này đảm bảo zone được giải phóng ngay sau khi đơn hết hạn.
     */
    private boolean hasMonthOverlap(LocalDate orderStart, LocalDate orderEnd, LocalDate queryStart, LocalDate queryEnd) {
        boolean hasOverlap = !orderEnd.isBefore(queryStart) && !orderStart.isAfter(queryEnd);
        // Debug log
        System.out.println("[DEBUG] Day overlap check:");
        System.out.println("  Order: " + orderStart + " to " + orderEnd);
        System.out.println("  Query: " + queryStart + " to " + queryEnd);
        System.out.println("  Has overlap: " + hasOverlap);
        return hasOverlap;
    }

//    @Override
//    public List<Order> getLast5Orders() {
//        return List.of();
//    }


}
