package com.f25_team6.duet.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tutor_notification_read",
  uniqueConstraints = @UniqueConstraint(columnNames = {"tutor_user_id","type","item_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationRead {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tutor_user_id", nullable = false)
  private Long tutorUserId;

  @Column(nullable = false, length = 24)
  private String type; // message|lesson|payment|review

  @Column(name = "item_id", nullable = false)
  private Long itemId;

  @Builder.Default
  private OffsetDateTime readAt = OffsetDateTime.now();
}
