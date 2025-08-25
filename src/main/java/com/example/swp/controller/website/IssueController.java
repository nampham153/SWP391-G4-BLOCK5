package com.example.swp.controller.website;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.swp.dto.IssueRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.Issue;
import com.example.swp.entity.Staff;
import com.example.swp.enums.IssueStatus;
import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.StaffRepository;
import com.example.swp.service.IssueService;
import com.example.swp.service.NotificationService;
import com.example.swp.service.OrderService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrderService orderService;

    // ----------- Trang danh sách issue (cho admin/staff, search + filter) -----------
    @GetMapping
    public String listAllIssues(
            Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Issue> issuesPage = issueService.searchAndFilterIssues(search, status, pageable);

        model.addAttribute("issues", issuesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", issuesPage.getTotalPages());
        model.addAttribute("pageSize", size);

        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("pendingCount", issueService.countByStatus(IssueStatus.Pending));
        model.addAttribute("progressCount", issueService.countByStatus(IssueStatus.In_Progress));
        model.addAttribute("resolvedCount", issueService.countByStatus(IssueStatus.Resolved));
        model.addAttribute("closedCount", issueService.countByStatus(IssueStatus.Closed));
        model.addAttribute("totalCount", issueService.countAll());

        return "customer-issue-list";
    }


    // ----------- Tạo mới Issue -----------
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("issueRequest", new IssueRequest());
        model.addAttribute("orders", orderService.getAllOrders());
        return "create-issue";
    }

    // API đơn giản trả về thông tin đơn để form fill tự động
    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public java.util.Map<String, Object> getOrderInfo(@PathVariable int id) {
        return orderService.getOrderById(id)
                .map(o -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", o.getId());
                    m.put("customerId", o.getCustomer() != null ? o.getCustomer().getId() : null);
                    // Prefer staff assigned directly to order; fallback to storage's staff
                    com.example.swp.entity.Staff staff = o.getStaff();
                    if (staff == null && o.getStorage() != null) {
                        staff = o.getStorage().getStaff();
                    }
                    m.put("staffId", staff != null ? staff.getStaffid() : null);
                    m.put("staffName", staff != null ? staff.getFullname() : null);
                    m.put("storageId", o.getStorage() != null ? o.getStorage().getStorageid() : null);
                    m.put("storageName", o.getStorage() != null ? o.getStorage().getStoragename() : null);
                    return m;
                })
                .orElseGet(java.util.HashMap::new);
    }

    // Nhận form submit
    @PostMapping("/create")
    public String createIssue(
            @ModelAttribute("issueRequest") @Valid com.example.swp.dto.IssueRequest issueRequest,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ) {
        model.addAttribute("orders", orderService.getAllOrders());

        if (bindingResult.hasErrors()) {
            // Có lỗi validate: trả lại form và hiển thị lỗi cạnh trường nhập!
            return "create-issue";
        }

        try {
            issueService.createIssue(issueRequest, "CUSTOMER");
            model.addAttribute("success", "Tạo Issue thành công!");
            model.addAttribute("issueRequest", new com.example.swp.dto.IssueRequest());

            // Thông báo cho customer (nếu đang đăng nhập)
            String email = (String) session.getAttribute("email");
            if (email != null) {
                Optional<Customer> customerOpt = customerRepository.findByEmail(email);
                customerOpt.ifPresent(c ->
                        notificationService.createNotification("Bạn vừa gửi yêu cầu hỗ trợ (Issue) thành công!", c)
                );
            }
        } catch (Exception e) {
            model.addAttribute("error", "Tạo Issue thất bại: " + e.getMessage());
        }
        model.addAttribute("orders", orderService.getAllOrders());
        return "create-issue";
    }


    @GetMapping("/my-issues")
    public String viewCustomerIssues(
            Model model,
            HttpSession session,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            model.addAttribute("issues", Collections.emptyList());
            return "customer-issue-list";
        }

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy khách hàng!");
            model.addAttribute("issues", Collections.emptyList());
            return "customer-issue-list";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Issue> issuesPage = issueService.getIssuesByCustomerId(customer.get().getId(), pageable);

        model.addAttribute("issues", issuesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", issuesPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("status", status);

        // Đếm theo trạng thái
        List<Issue> allIssues = issueService.getIssuesByCustomerId(customer.get().getId());
        model.addAttribute("pendingCount", allIssues.stream().filter(i -> i.getStatus() == IssueStatus.Pending).count());
        model.addAttribute("progressCount", allIssues.stream().filter(i -> i.getStatus() == IssueStatus.In_Progress).count());
        model.addAttribute("resolvedCount", allIssues.stream().filter(i -> i.getStatus() == IssueStatus.Resolved).count());
        model.addAttribute("closedCount", allIssues.stream().filter(i -> i.getStatus() == IssueStatus.Closed).count());
        model.addAttribute("totalCount", allIssues.size());

        return "customer-issue-list";
    }

    // ----------- Xem chi tiết Issue -----------
    @GetMapping("/{id}/view")
    public String viewIssue(@PathVariable("id") int id, Model model) {
        Optional<Issue> issueOpt = issueService.getIssueById(id);
        if (issueOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy Issue!");
            return "issue-view";
        }
        model.addAttribute("issue", issueOpt.get());
        return "issue-view";
    }

    // ----------- Hiển thị form sửa Issue -----------
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") int id, Model model) {
        Optional<Issue> issueOpt = issueService.getIssueById(id);
        if (issueOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy Issue!");
            return "issue-edit";
        }
        model.addAttribute("issue", issueOpt.get());
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("staffs", staffRepository.findAll());
        return "issue-edit";
    }

    @PostMapping("/edit")
    public String editIssue(@ModelAttribute("issue") @Valid Issue issue,
                            BindingResult bindingResult,
                            Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("staffs", staffRepository.findAll());
        if (bindingResult.hasErrors()) {
            return "issue-edit";
        }
        issueService.saveIssue(issue);
        model.addAttribute("success", "Cập nhật Issue thành công!");
        return "redirect:/issues/my-issues";
    }

    // ----------- Xóa Issue -----------
    @PostMapping("/delete")
    public String deleteIssue(@RequestParam("id") int id, Model model) {
        issueService.deleteIssueById(id);
        return "redirect:/issues/my-issues";
    }

    // ----------- Trang tổng hợp Issue -----------
    @GetMapping("/summary")
    public String summary(Model model) {
        long total = issueService.countAll();
        long choXuLy = issueService.countByStatus(IssueStatus.Pending);
        long dangXuLy = issueService.countByStatus(IssueStatus.In_Progress);
        long daGiaiQuyet = issueService.countByStatus(IssueStatus.Resolved);
        long daDong = issueService.countByStatus(IssueStatus.Closed);

        model.addAttribute("total", total);
        model.addAttribute("choXuLy", choXuLy);
        model.addAttribute("dangXuLy", dangXuLy);
        model.addAttribute("daGiaiQuyet", daGiaiQuyet);
        model.addAttribute("daDong", daDong);

        return "issue-summary";
    }

    @GetMapping("/report")
    @PreAuthorize("hasAuthority('STAFF')")
    public String showReportForm(Model model) {
        List<Issue> issues = issueService.getAllIssues();
        model.addAttribute("issues", issues);
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("staffs", staffRepository.findAll());
        return "staff-report";
    }

    @PostMapping("/report")
    @PreAuthorize("hasAuthority('STAFF')")
    public String submitReport(
            @RequestParam(required = false) List<Long> issueIds,
            @RequestParam(required = false) String customReport,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer assignedStaffId,
            Model model
    ) {
        try {
            if (customReport != null && !customReport.trim().isEmpty() && assignedStaffId != null) {
                IssueRequest issueRequest = new IssueRequest();
                issueRequest.setSubject("Báo cáo từ nhân viên");
                issueRequest.setDescription(customReport);
                issueRequest.setCustomerId(customerId); // có thể null cho vấn đề nội bộ
                issueRequest.setAssignedStaffId(assignedStaffId);

                Issue issue = issueService.createIssue(issueRequest, "STAFF");
                issue.setCreatedByType("STAFF");
                issueService.save(issue);
            }

            model.addAttribute("success", "Gửi báo cáo thành công!");
            model.addAttribute("issues", issueService.getAllIssues());
            model.addAttribute("customReport", "");
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("staffs", staffRepository.findAll());

            return "staff-report";
        } catch (Exception e) {
            model.addAttribute("error", "Gửi báo cáo thất bại: " + e.getMessage());
            model.addAttribute("issues", issueService.getAllIssues());
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("staffs", staffRepository.findAll());
            return "staff-report";
        }
    }




    @GetMapping("/staff")
    @PreAuthorize("hasAuthority('STAFF')")
    public String showListStaffIssue(Model model) {
        List<Issue> issues = issueService.getAllIssues();
        if (issues == null) {
            issues = new ArrayList<>();
        }
        // Chỉ hiển thị issues được tạo bởi nhân viên
        issues = issues.stream()
                .filter(issue -> "STAFF".equals(issue.getCreatedByType()))
                .collect(Collectors.toList());
        model.addAttribute("issues", issues);
        return "staff-issue-list";
    }

    @GetMapping("/staff/view/{id}")
    @PreAuthorize("hasAuthority('STAFF')")
    public String viewStaffIssue(@PathVariable int id, Model model) {
        Optional<Issue> issueOpt = issueService.getIssueById(id);
        if (issueOpt.isPresent()) {
            model.addAttribute("issue", issueOpt.get());
            return "staff-issue-detail";
        }
        return "redirect:/issues/staff";
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('STAFF')")
    public String issueDetail(@PathVariable("id") int id, Model model) {
        Issue issue = issueService.getIssueById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy issue!"));
        List<Staff> staffs = staffRepository.findAll();
        model.addAttribute("issue", issue);
        model.addAttribute("staffs", staffs);
        return "staff-report-detail";
    }

    @PostMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('STAFF')")
    public String updateIssueDetail(
            @PathVariable("id") int id,
            @RequestParam("assignedStaffId") int assignedStaffId,
            @RequestParam("resolved") Boolean resolved,
            Model model) {
        issueService.updateAssignedStaffAndStatus(id, assignedStaffId, resolved);

        Issue issue = issueService.getIssueById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy issue!"));
        List<Staff> staffs = staffRepository.findAll();
        model.addAttribute("issue", issue);
        model.addAttribute("staffs", staffs);
        model.addAttribute("success", "Cập nhật thành công!");
        return "staff-report-detail";
    }



}
