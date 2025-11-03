package com.journalsystem.controller;

import com.journalsystem.model.Message;
import com.journalsystem.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Message>> getMessagesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getMessagesByUserId(userId));
    }

    @GetMapping("/received/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Message>> getReceivedMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getReceivedMessages(userId));
    }

    @GetMapping("/sent/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Message>> getSentMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getSentMessages(userId));
    }

    @GetMapping("/unread/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Message>> getUnreadMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getUnreadMessages(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    @GetMapping("/{id}/replies")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Message>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getReplies(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.createMessage(message));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Message> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
