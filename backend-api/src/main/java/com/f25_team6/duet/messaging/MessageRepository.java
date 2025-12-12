package com.f25_team6.duet.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

  Message findTop1ByConversationIdOrderByCreatedAtDesc(Long conversationId);

  List<Message> findTop50ByConversation_Tutor_IdOrderByCreatedAtDesc(Long tutorId);

  long countByConversationIdAndSender_IdNot(Long conversationId, Long senderId);

  long countByConversationIdAndCreatedAtAfterAndSender_IdNot(Long conversationId, java.time.OffsetDateTime createdAfter,
      Long senderId);

  void deleteByConversationId(Long conversationId);
}
