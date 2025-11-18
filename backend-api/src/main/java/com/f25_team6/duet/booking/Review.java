package com.f25_team6.duet.booking;

import com.f25_team6.duet.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Table(name="reviews", uniqueConstraints=@UniqueConstraint(columnNames="lesson_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional=false) @JoinColumn(name="lesson_id")
  private Lesson lesson;

  @ManyToOne(optional=false) @JoinColumn(name="tutor_user_id")
  private User tutor;

  @ManyToOne(optional=false) @JoinColumn(name="reviewer_student_user_id")
  private User reviewerStudent;

  @Min(1) @Max(5) @Column(nullable=false)
  private Integer rating;

  @Column(columnDefinition="text")
  private String text;


  @Builder.Default
  @Column(nullable=false) private OffsetDateTime createdAt = OffsetDateTime.now();
}
