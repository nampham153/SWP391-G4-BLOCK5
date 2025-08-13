package com.example.swp.entity;


import com.example.swp.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Date;
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
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
    }



