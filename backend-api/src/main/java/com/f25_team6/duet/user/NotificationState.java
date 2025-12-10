package com.f25_team6.duet.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tutor_notification_state")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationState {
  @Id
  @Column(name = "tutor_user_id")
  private Long tutorUserId;

  private OffsetDateTime lastReadAllAt;
}
