package com.f25_team6.duet.messaging;

import com.f25_team6.duet.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="conversations",
  uniqueConstraints=@UniqueConstraint(columnNames={"student_user_id","tutor_user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="student_user_id")
  private User student;

  @ManyToOne(optional=false) @JoinColumn(name="tutor_user_id")
  private User tutor;
}
