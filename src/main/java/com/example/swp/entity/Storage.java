package com.example.swp.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storageid;

    private String storagename;
    private String address;
    private String city;
    private String state;

    private double area;
    private double pricePerDay;
    private String description;
    private String imUrl;

    private boolean status;

    // private String imageUrl;
    public Storage(Integer id) {
        this.storageid = id;
    }

    @OneToMany(mappedBy = "storage")
    @JsonIgnore
    private List<Contact> contacts;

    @OneToMany(mappedBy = "storage")
    @JsonIgnore
    private List<StorageTransaction> storageTransactions;

    @ManyToOne
    @JoinColumn(name = "staffid", referencedColumnName = "staffid")
    @JsonIgnoreProperties({"contacts", "feedbacks", "attendances"})
    private Staff staff;

}
