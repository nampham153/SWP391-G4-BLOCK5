package com.example.swp.entity;

import com.example.swp.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    private Manager manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "customer_unread", nullable = false)
    private boolean customerUnread = false;

    @Column(name = "manager_unread", nullable = false)
    private boolean managerUnread = false;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    // Helper methods
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void markAsReadByCustomer() {
        this.customerUnread = false;
    }

    public void markAsReadByManager() {
        this.managerUnread = false;
    }

    public void markAsUnreadForCustomer() {
        this.customerUnread = true;
    }

    public void markAsUnreadForManager() {
        this.managerUnread = true;
    }
}
