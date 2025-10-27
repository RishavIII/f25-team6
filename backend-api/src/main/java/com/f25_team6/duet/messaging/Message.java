package com.f25_team6.duet.messaging;

import com.f25_team6.duet.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="conversation_id")
  private Conversation conversation;

  @ManyToOne(optional=false) @JoinColumn(name="sender_user_id")
  private User sender;

  @Column(nullable=false, columnDefinition="text")
  private String body;

  @Builder.Default
  @Column(nullable=false) private boolean isAbusiveFlag = false;

  @Builder.Default
  @Column(nullable=false) private OffsetDateTime createdAt = OffsetDateTime.now();
}
