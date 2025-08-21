package com.example.swp.controller.api;

import com.example.swp.dto.OrderRequest;
import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import com.example.swp.repository.StorageRepository;
import com.example.swp.service.OrderService;
import com.example.swp.service.StorageTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/SWP")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageTransactionService storageTransactionService;

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> findById(@PathVariable int id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Order order = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/orders/{id}/mark-paid")
    public ResponseEntity<?> markOrderAsPaid(@PathVariable int id) {
        try {
            orderService.markOrderAsPaid(id);
            return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đơn hàng #" + id + " là PAID và tạo giao dịch kho."));
        } catch (Exception e) {
            log.error("Error marking order as paid: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }




    @PostMapping("/calculate-total")
    public ResponseEntity<Map<String, Object>> calculateTotal(@RequestBody Map<String, Object> request) {
        try {
            // Parse request data
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");
            Integer storageId = (Integer) request.get("storageId");

            if (startDateStr == null || endDateStr == null || storageId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Thiếu thông tin bắt buộc"));
            }

            // Parse dates
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            // Validate dates
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ngày kết thúc phải sau ngày bắt đầu"));
            }

            // Get storage info
            Optional<Storage> storageOpt = storageRepository.findById(storageId);
            if (!storageOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không tìm thấy kho"));
            }

            Storage storage = storageOpt.get();

            // Calculate using same logic as OrderService
            long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            if (rentalDays <= 0) {
                rentalDays = 1;
            }

            double totalAmount = rentalDays * storage.getPricePerDay();

            // Return result
            Map<String, Object> response = new HashMap<>();
            response.put("totalAmount", totalAmount);
            response.put("days", rentalDays);
            response.put("pricePerDay", storage.getPricePerDay());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error calculating total: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tính toán: " + e.getMessage()));
        }
    }
}