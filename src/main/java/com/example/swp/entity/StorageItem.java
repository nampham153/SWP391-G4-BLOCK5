package com.example.swp.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StorageItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String itemName;

    private int quantity;
    private double volumePerUnit;

    private LocalDate dateStored;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String note;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}

