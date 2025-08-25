package com.example.swp.entity;


import java.util.Date;

import com.example.swp.enums.IssueStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    public class Issue {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        private String subject;

        private Date createdDate;
        private boolean resolved;
        @Column(length = 500)
        private String description;

        @Enumerated(EnumType.STRING)
        private IssueStatus status;


        @ManyToOne
        @JoinColumn(name = "customer_id")
        private Customer customer;

        @ManyToOne
        @JoinColumn(name = "staff_id")
        private Staff assignedStaff;

        @Column(name = "created_by_type", length = 20)
        private String createdByType;

        // Explicit getter and setter methods for fields that are causing compilation errors
        public String getCreatedByType() {
            return createdByType;
        }

        public void setCreatedByType(String createdByType) {
            this.createdByType = createdByType;
        }

        public IssueStatus getStatus() {
            return status;
        }

        public void setStatus(IssueStatus status) {
            this.status = status;
        }
    }



