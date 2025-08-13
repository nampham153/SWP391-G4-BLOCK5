//package com.example.swp.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.Instant;
//import java.util.UUID;
//import java.util.List;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//@Table(name = "chat_session")
//public class ChatSession {
//
//    @Id
//    @GeneratedValue
//    private UUID id;
//
//    @Column(nullable = false, updatable = false)
//    private Instant createdAt = Instant.now();
//
//    // Thêm nếu muốn biết session này là giữa ai với ai
//    @Column(nullable = false)
//    private String userId;
//
//    @Column(nullable = false)
//    private String managerId;
//
//    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatMessage> messages;
//}
