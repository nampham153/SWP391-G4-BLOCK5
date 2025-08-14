package com.example.swp.controller.website;

import com.example.swp.annotation.LogActivity;
import com.example.swp.dto.VoucherDTO;
import com.example.swp.entity.Voucher;
import com.example.swp.entity.VoucherUsage;
import com.example.swp.enums.VoucherStatus;
import com.example.swp.service.VoucherService;
import com.example.swp.service.VoucherUsageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/SWP")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherUsageService voucherUsageService;

    @GetMapping("/staff/vouchers")
    public String showAllVoucherList(Model model) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        model.addAttribute("vouchers", vouchers);
        return "all-vouchers";
    }

    @GetMapping("/staff/addvoucher")
    public String showAddVoucherForm(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        return "add-voucher";
    }

    @LogActivity(action = "Tạo voucher mới")
    @PostMapping("/staff/addvoucher")
    public String addVoucher(@Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        // Kiểm tra ngày bắt đầu < ngày kết thúc
        if (voucherDTO.getStartDate() != null && voucherDTO.getEndDate() != null &&
                !voucherDTO.getEndDate().isAfter(voucherDTO.getStartDate())) {
            bindingResult.rejectValue("endDate", "error.voucher", "Ngày kết thúc phải sau ngày bắt đầu");
        }

        if (bindingResult.hasErrors()) {
            // Nếu có lỗi validate, trả lại form với thông báo lỗi
            model.addAttribute("voucher", voucherDTO);
            return "add-voucher";
        }

        // Map DTO -> Entity
        Voucher voucher = new Voucher();
        voucher.setName(voucherDTO.getName());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setDiscountAmount(voucherDTO.getDiscountAmount());
        voucher.setRequiredPoint(voucherDTO.getRequiredPoint());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        voucher.setTotalQuantity(voucherDTO.getTotalQuantity());
        voucher.setRemainQuantity(voucherDTO.getRemainQuantity());
        voucher.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());

        voucherService.saveVoucher(voucher);
        redirectAttributes.addFlashAttribute("success", "Thêm voucher thành công!");
        return "redirect:/SWP/staff/vouchers";
    }
    @GetMapping("/staff/vouchers/{id}/edit")
    public String showEditVoucherForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (voucherOpt.isPresent()) {
            model.addAttribute("voucher", voucherOpt.get());
            model.addAttribute("editMode", true); // <-- thêm cờ editMode
            return "staff-edit-voucher";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher!");
            return "redirect:/SWP/staff/vouchers";
        }
    }


    @LogActivity(action = "Cập nhật voucher")
    @PostMapping("/staff/vouchers/{id}/edit")
    public String editVoucher(@PathVariable Integer id,
                              @Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        // Kiểm tra ngày bắt đầu < ngày kết thúc
        if (voucherDTO.getStartDate() != null && voucherDTO.getEndDate() != null &&
                !voucherDTO.getEndDate().isAfter(voucherDTO.getStartDate())) {
            bindingResult.rejectValue("endDate", "error.voucher", "Ngày kết thúc phải sau ngày bắt đầu");
        }

        // ✅ Kiểm tra remainQuantity <= totalQuantity
        if (voucherDTO.getRemainQuantity() != null && voucherDTO.getTotalQuantity() != null &&
                voucherDTO.getRemainQuantity() > voucherDTO.getTotalQuantity()) {
            bindingResult.rejectValue("remainQuantity", "error.voucher",
                    "Số lượng còn lại không thể lớn hơn tổng số lượng phát hành");
        }

        if (bindingResult.hasErrors()) {
            voucherDTO.setId(id);
            model.addAttribute("voucher", voucherDTO);
            model.addAttribute("editMode", true); // <-- bật form khi có lỗi
            return "staff-edit-voucher";
        }

        Optional<Voucher> oldVoucherOpt = voucherService.getVoucherById(id);
        if (oldVoucherOpt.isPresent()) {
            Voucher oldVoucher = oldVoucherOpt.get();
            oldVoucher.setName(voucherDTO.getName());
            oldVoucher.setDescription(voucherDTO.getDescription());
            oldVoucher.setDiscountAmount(voucherDTO.getDiscountAmount());
            oldVoucher.setRequiredPoint(voucherDTO.getRequiredPoint());
            oldVoucher.setStartDate(voucherDTO.getStartDate());
            oldVoucher.setEndDate(voucherDTO.getEndDate());
            oldVoucher.setTotalQuantity(voucherDTO.getTotalQuantity());
            oldVoucher.setRemainQuantity(voucherDTO.getRemainQuantity());
            oldVoucher.setStatus(VoucherStatus.valueOf(voucherDTO.getStatus()));
            oldVoucher.setUpdatedAt(LocalDateTime.now());
            voucherService.saveVoucher(oldVoucher);
            redirectAttributes.addFlashAttribute("success", "Cập nhật voucher thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher để cập nhật!");
        }

        return "redirect:/SWP/staff/vouchers";
    }



    @LogActivity(action = "Xóa voucher")
    @PostMapping("/staff/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("success", "Xóa voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa voucher: " + e.getMessage());
        }
        return "redirect:/SWP/staff/vouchers";
    }

    @PostMapping("/staff/vouchers/{id}/toggle-status")
    public String toggleVoucherStatus(@PathVariable Integer id,
                                      @RequestParam(required = false) String returnUrl,
                                      RedirectAttributes redirectAttributes) {
        Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            if (voucher.getStatus() == VoucherStatus.ACTIVE) {
                voucher.setStatus(VoucherStatus.INACTIVE);
            } else {
                voucher.setStatus(VoucherStatus.ACTIVE);
            }
            voucher.setUpdatedAt(LocalDateTime.now());
            voucherService.saveVoucher(voucher);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái voucher thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher để cập nhật trạng thái.");
        }

        return "redirect:" + (returnUrl != null ? returnUrl : "/SWP/staff/vouchers");
    }

    @GetMapping("/staff/voucher-usage")
    public String showVoucherUsageHistory(Model model) {
        List<VoucherUsage> usageHistories = voucherUsageService.findAll();
        model.addAttribute("usageHistories", usageHistories);
        return "staff-voucher-usage";
    }

}