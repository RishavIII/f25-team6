package com.f25_team6.duet.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
  Optional<Conversation> findByStudentIdAndTutorId(Long studentId, Long tutorId);

  List<Conversation> findByTutor_Id(Long tutorId);

  List<Conversation> findByStudent_Id(Long studentId);

  void deleteByStudent_Id(Long studentId);

  void deleteByTutor_Id(Long tutorId);
}
