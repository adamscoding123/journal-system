package com.journalsystem.repository;

import com.journalsystem.model.Message;
import com.journalsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    List<Message> findByReceiverOrderBySentAtDesc(User receiver);
    List<Message> findBySenderOrReceiverOrderBySentAtDesc(User sender, User receiver);
    List<Message> findByReceiverAndIsReadFalse(User receiver);
    List<Message> findByParentMessage(Message parentMessage);
}
