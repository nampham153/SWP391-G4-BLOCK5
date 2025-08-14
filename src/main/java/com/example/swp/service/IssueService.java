package com.example.swp.service;

import com.example.swp.dto.IssueRequest;
import com.example.swp.entity.Issue;
import com.example.swp.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IssueService {
    List<Issue> getAllIssues();
    Optional<Issue> getIssueById(int id);
    Issue createIssue(IssueRequest issueRequest, String createdByType);
    List<Issue> getIssuesByCustomerId(int customerId);
    Issue getIssueByIdOrThrow(int id);
    Issue saveIssue(Issue issue);
    void deleteIssueById(int id);
    long countAll();
    long countByStatus(IssueStatus status);

    // *** THÊM DÒNG NÀY ***


    void updateAssignedStaffAndStatus(int id, int staffId, Boolean resolved);

    void save(Issue issue);
    Page<Issue> searchAndFilterIssues(String search, String status, Pageable pageable);

    Page<Issue> getIssuesByCustomerId(int customerId, Pageable pageable);

}
