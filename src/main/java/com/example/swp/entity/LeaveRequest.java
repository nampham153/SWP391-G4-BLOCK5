package com.example.swp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private LocalDate fromDate;
    private LocalDate toDate;
    private String leaveType;      // Annual, Personal, Sick, etc.
    private String reason;

    @Enumerated(EnumType.STRING)
    private Status status;         // CHO_DUYET, DUYET, TU_CHOI

    private String managerNote;
    private LocalDateTime createdAt;

    public enum Status {
        CHO_DUYET, DUYET, TU_CHOI
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.CHO_DUYET;
    }


}
