package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.entity.StorageTransaction;
import com.example.swp.enums.TransactionType;
import com.example.swp.service.CustomerService;
import com.example.swp.service.OrderService;
import com.example.swp.repository.FeedbackRepository;
import com.example.swp.service.StorageTransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/SWP/customers")
public class CustomerDetailController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private StorageTransactionService storageTransactionService;

    // /SWP/customers/my-transactions
    @GetMapping("/my-transactions")
    public String viewMyTransactions(@RequestParam(value = "type", required = false) String type,
                                     @RequestParam(value = "sort", defaultValue = "newest") String sort,
                                     Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) return "redirect:/login";

        List<StorageTransaction> transactions = storageTransactionService.findByCustomerId(customer.getId());

        if (type != null && !type.isBlank()) {
            String t = type.trim().toUpperCase();
            transactions = transactions.stream()
                    .filter(tx -> tx.getType() != null && tx.getType().name().equalsIgnoreCase(t))
                    .collect(Collectors.toList());
        }

        Comparator<StorageTransaction> byDate =
                Comparator.comparing(StorageTransaction::getTransactionDate, Comparator.nullsLast(Comparator.naturalOrder()));

        transactions.sort("oldest".equalsIgnoreCase(sort) ? byDate : byDate.reversed());

        model.addAttribute("transactions", transactions);
        model.addAttribute("customer", customer);
        model.addAttribute("type", type == null ? "" : type);
        model.addAttribute("sort", sort.toLowerCase());
        return "my-transactions";
    }
}
