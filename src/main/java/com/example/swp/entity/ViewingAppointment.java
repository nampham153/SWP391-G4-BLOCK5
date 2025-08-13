package com.example.swp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "viewing_appointment", schema = "dbo")
public class ViewingAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String customerName;
    private String phone;
    private String email;

    private LocalDateTime appointmentDateTime;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String viewingPurpose;

    private String note;

    @ManyToOne
    @JoinColumn(name = "storage_id")
    private Storage storage;

    @Column(nullable = false)
    private String status = "PENDING"; // ACCEPTED, REJECTED

    @Column(columnDefinition = "NVARCHAR(255)")
    private String rejectReason;
}
