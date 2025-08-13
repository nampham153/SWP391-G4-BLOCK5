package com.example.swp.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class SupportActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String activityType; // "Password Recovery" hoặc "Profile Access"
    private String status;       // "Resolved", "Failed", etc.
    private Date activityTime;   // <-- Đã đúng kiểu java.util.Date

    @ManyToOne
    private Customer customer;

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getActivityTime() { return activityTime; }
    public void setActivityTime(Date activityTime) { this.activityTime = activityTime; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
