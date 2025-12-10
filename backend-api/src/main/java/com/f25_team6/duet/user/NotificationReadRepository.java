package com.f25_team6.duet.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {
  boolean existsByTutorUserIdAndTypeAndItemId(Long tutorUserId, String type, Long itemId);
  long countByTutorUserIdAndTypeAndItemId(Long tutorUserId, String type, Long itemId);
}
