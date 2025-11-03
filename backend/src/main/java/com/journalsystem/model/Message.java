package com.journalsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String subject;

    @Column(length = 5000, nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
