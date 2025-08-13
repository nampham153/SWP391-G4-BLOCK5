package com.example.swp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WishList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"wishLists", "contacts", "feedbacks", "storageTransactions", "payments", "authorities"})
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "storage_id")
    @JsonIgnoreProperties({"wishLists", "contacts", "storageTransactions"})
    private Storage storage;
}
