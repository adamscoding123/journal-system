package com.journalsystem.service;

import com.journalsystem.model.Message;
import com.journalsystem.model.User;
import com.journalsystem.repository.MessageRepository;
import com.journalsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public List<Message> getMessagesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.findBySenderOrReceiverOrderBySentAtDesc(user, user);
    }

    public List<Message> getReceivedMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.findByReceiverOrderBySentAtDesc(user);
    }

    public List<Message> getSentMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.findBySenderOrderBySentAtDesc(user);
    }

    public List<Message> getUnreadMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.findByReceiverAndIsReadFalse(user);
    }

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    public Message markAsRead(Long id) {
        Message message = getMessageById(id);
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    public List<Message> getReplies(Long parentMessageId) {
        Message parentMessage = getMessageById(parentMessageId);
        return messageRepository.findByParentMessage(parentMessage);
    }
}
