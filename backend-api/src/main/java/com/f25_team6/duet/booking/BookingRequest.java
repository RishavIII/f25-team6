package com.f25_team6.duet.booking;

import com.f25_team6.duet.catalog.Instrument;
import com.f25_team6.duet.common.enums.*;
import com.f25_team6.duet.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="booking_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingRequest {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="student_user_id")
  private User student;

  @ManyToOne(optional=false) @JoinColumn(name="tutor_user_id")
  private User tutor;

  @ManyToOne(optional=false) @JoinColumn(name="instrument_id")
  private Instrument instrument;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private Level level;

  @Min(15) @Max(240)
  @Column(nullable=false)
  private Integer durationMin;

  @Column(nullable=false)
  private OffsetDateTime requestedStartUtc;

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private LessonMode lessonMode;

  @Builder.Default
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
  private BookingStatus status = BookingStatus.PENDING;

  private OffsetDateTime altProposedStartUtc;

  @Column(columnDefinition="text")
  private String notes;

  @Builder.Default
  @Column(nullable=false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @Builder.Default
  @Column(nullable=false)
  private OffsetDateTime updatedAt = OffsetDateTime.now();
}
