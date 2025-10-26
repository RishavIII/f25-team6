package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.LessonMode;
import com.f25_team6.duet.common.enums.LessonStatus;
import com.f25_team6.duet.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="lessons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lesson {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="tutor_user_id")
  private User tutor;

  @ManyToOne(optional=false) @JoinColumn(name="student_user_id")
  private User student;

  @OneToOne(optional=false) @JoinColumn(name="booking_request_id", unique=true)
  private BookingRequest bookingRequest;

  @Column(nullable=false) private OffsetDateTime startUtc;
  @Column(nullable=false) private OffsetDateTime endUtc;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private LessonMode lessonMode;

  @Column(nullable=false) private Integer priceCents;

  @Builder.Default
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private LessonStatus status = LessonStatus.SCHEDULED;

  private String canceledReason;

  @Builder.Default
  private Integer cancellationFeeCents = 0;


  @Builder.Default
  @Column(nullable=false) private OffsetDateTime createdAt = OffsetDateTime.now();
  @Builder.Default
  @Column(nullable=false) private OffsetDateTime updatedAt = OffsetDateTime.now();
}
