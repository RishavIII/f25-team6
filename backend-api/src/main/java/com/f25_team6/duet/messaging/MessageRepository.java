package com.f25_team6.duet.messaging;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
