package com.example.swp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.util.List;

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

    private boolean status; // true: còn trống, false: đang bị thuê
    private Double latitude;
    private Double longitude;

    //    private String imageUrl; // hoặc dùng List<StorageImage> nếu nhiều ảnh
public Storage(Integer id) {
    this.storageid = id;
}


    @OneToMany(mappedBy = "storage")
    @JsonIgnore
    private List<Contact> contacts;
    @JsonIgnore
    @OneToMany(mappedBy = "storage")
    private List<StorageTransaction> storageTransactions;


}

