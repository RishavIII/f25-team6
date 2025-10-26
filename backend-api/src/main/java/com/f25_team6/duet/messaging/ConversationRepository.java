// com/f25_team6/duet/messaging/ConversationRepository.java
package com.f25_team6.duet.messaging;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  Optional<Conversation> findByStudentIdAndTutorId(Long studentId, Long tutorId);
}
