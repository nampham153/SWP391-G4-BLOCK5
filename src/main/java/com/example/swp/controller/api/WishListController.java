package com.example.swp.controller.api;

import com.example.swp.dto.WishlistRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.Storage;
import com.example.swp.entity.WishList;
import com.example.swp.repository.StorageRepository;
import com.example.swp.service.WishListService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {

    @Autowired
    private WishListService wishListService;
    @Autowired
    private StorageRepository storageRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addToWishList(@RequestBody WishlistRequest request, HttpSession session) {
        Long storageId = request.getStorageId();
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập");
        }

        Optional<Storage> storageOpt = storageRepository.findById(storageId.intValue());
        if (storageOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy kho");
        }

        // Check if already in wishlist
        List<WishList> existingWishlist = wishListService.getWishListByCustomer(customer);
        boolean alreadyExists = existingWishlist.stream()
                .anyMatch(w -> w.getStorage() != null &&
                        w.getStorage().getStorageid() == storageOpt.get().getStorageid());

        if (alreadyExists) {
            return ResponseEntity.badRequest().body("Kho đã có trong wishlist");
        }

        wishListService.addToWishList(customer, storageOpt.get());
        return ResponseEntity.ok("Đã thêm vào wishlist");
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromWishList(@RequestBody WishlistRequest request, HttpSession session) {
        Long storageId = request.getStorageId();
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập");
        }

        Optional<Storage> storageOpt = storageRepository.findById(storageId.intValue());
        if (storageOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy kho");
        }

        wishListService.removeFromWishList(customer, storageOpt.get());
        return ResponseEntity.ok("Đã xóa khỏi wishlist");
    }

    @GetMapping("/my")
    public ResponseEntity<?> getWishList(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập");
        }

        List<WishList> wishLists = wishListService.getWishListByCustomer(customer);
        return ResponseEntity.ok(wishLists);
    }
}

// Separate controller for Thymeleaf pages
@Controller
@RequestMapping("/SWP")
class WishListViewController {

    @GetMapping("/wishlist")
    public String wishlistPage() {
        return "wishlist"; // returns wishlist.html template
    }
}