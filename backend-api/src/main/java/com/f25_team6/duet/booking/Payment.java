// com/f25_team6/duet/booking/Payment.java
package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.PaymentStatus;
import com.f25_team6.duet.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
@Entity @Table(name="payments", uniqueConstraints=@UniqueConstraint(columnNames="lesson_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional=false) @JoinColumn(name="lesson_id")
  private Lesson lesson;

  @ManyToOne(optional=false) @JoinColumn(name="student_user_id")
  private User student;
  @Column(nullable=false) private Integer amountCents;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private PaymentStatus status;
  private String processorRef;

  @Builder.Default
  @Column(nullable=false) private OffsetDateTime createdAt = OffsetDateTime.now();
}
