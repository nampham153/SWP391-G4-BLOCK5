package com.example.swp.service.impl;

import com.example.swp.dto.IssueRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.Issue;
import com.example.swp.entity.Staff;
import com.example.swp.enums.IssueStatus;
import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.IssueRepository;
import com.example.swp.repository.StaffRepository;
import com.example.swp.service.ActivityLogService;
import com.example.swp.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Override
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    @Override
    public Optional<Issue> getIssueById(int id) {
        return issueRepository.findById(id);
    }

    @Override
    public Issue getIssueByIdOrThrow(int id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue với id " + id));
    }

    @Override
    public Issue createIssue(IssueRequest issueRequest, String createdByType) {
        Customer customer = null;
        if (issueRequest.getCustomerId() != null) {
            customer = customerRepository.findById(issueRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id " + issueRequest.getCustomerId()));
        }

        Staff staff = staffRepository.findById(issueRequest.getAssignedStaffId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff với id " + issueRequest.getAssignedStaffId()));

        Issue issue = new Issue();
        issue.setSubject(issueRequest.getSubject());
        issue.setDescription(issueRequest.getDescription());
        issue.setCustomer(customer); // có thể null cho vấn đề nội bộ
        issue.setAssignedStaff(staff);
        issue.setCreatedDate(new Date());
        issue.setResolved(false);
        issue.setCreatedByType(createdByType);
        issue.setStatus(IssueStatus.Pending);

        Issue savedIssue = issueRepository.save(issue);

        // Ghi lại activity log
        activityLogService.logActivity(
                "Gửi yêu cầu hỗ trợ",
                "Khách hàng " + customer.getFullname()
                        + " gửi yêu cầu hỗ trợ: " + savedIssue.getSubject()
                        + " - Mô tả: " + savedIssue.getDescription()
                        + " - Được phân công cho: " + staff.getFullname(),
                customer,
                null, null, null, null,
                savedIssue
        );
        return savedIssue;
    }



    @Override
    public void updateAssignedStaffAndStatus(int id, int staffId, Boolean resolved) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy issue với id " + id));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff với id " + staffId));
        issue.setAssignedStaff(staff);
        issue.setResolved(resolved);
        issueRepository.save(issue);
    }

    @Override
    public void save(Issue issue) {
        issueRepository.save(issue);
    }

    @Override
    public List<Issue> getIssuesByCustomerId(int customerId) {
        return issueRepository.findByCustomerId(customerId);
    }

    @Override
    public Issue saveIssue(Issue issue) {
        return issueRepository.save(issue);
    }

    @Override
    public void deleteIssueById(int id) {
        issueRepository.deleteById(id);
    }

    @Override
    public long countAll() {
        return issueRepository.count();
    }

    @Override
    public long countByStatus(IssueStatus status) {
        return issueRepository.countByStatus(status);
    }
    @Override




    public Page<Issue> searchAndFilterIssues(String search, String status, Pageable pageable) {
        if ((search == null || search.isBlank()) && (status == null || status.isBlank()))
            return issueRepository.findAll(pageable);

        IssueStatus st = null;
        if (status != null && !status.isBlank()) {
            try { st = IssueStatus.valueOf(status); } catch (Exception ignored) {}
        }

        if (search != null && !search.isBlank() && st != null)
            return issueRepository.searchByKeywordAndStatusPaged(search, st, pageable);
        if (search != null && !search.isBlank())
            return issueRepository.searchByKeywordPaged(search, pageable);
        if (st != null)
            return issueRepository.findByStatus(st, pageable);

        return issueRepository.findAll(pageable);
    }

    @Override
    public Page<Issue> getIssuesByCustomerId(int customerId, Pageable pageable) {
        return issueRepository.findByCustomerId(customerId, pageable);
    }


}