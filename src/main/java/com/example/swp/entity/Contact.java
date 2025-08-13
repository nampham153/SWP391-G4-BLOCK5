package com.example.swp.entity;

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
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private boolean status;
    private Date dateFrom;
    private Date dateTo;
    private double amount;
    //customer 1-many
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    //staff 1-many
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;
    //storage 1-many
    @ManyToOne
    @JoinColumn(name = "storage_id")
    private Storage storage;
}
