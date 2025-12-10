package com.f25_team6.duet.user;

import com.f25_team6.duet.booking.*;
import com.f25_team6.duet.common.enums.BookingStatus;
import com.f25_team6.duet.common.enums.LessonStatus;
import com.f25_team6.duet.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tutors/{tutorId}/dashboard")
@RequiredArgsConstructor
public class TutorDashboardController {

  private final LessonRepository lessonRepo;
  private final BookingRequestRepository bookingRepo;
  private final PaymentRepository paymentRepo;
  private final ReviewRepository reviewRepo;
  private final TutorProfileRepository tutorProfileRepo;

  @GetMapping("/upcoming-lessons")
  public List<UpcomingLessonDto> upcoming(@PathVariable Long tutorId,
                                          @RequestParam(defaultValue = "7") int limit) {
    OffsetDateTime now = OffsetDateTime.now();
    return lessonRepo
        .findTop10ByTutor_IdAndStatusAndStartUtcAfterOrderByStartUtcAsc(tutorId, LessonStatus.SCHEDULED, now)
        .stream()
        .limit(Math.max(1, limit))
        .map(UpcomingLessonDto::from)
        .collect(Collectors.toList());
  }

  @GetMapping("/reviews")
  public List<ReviewSummaryDto> recentReviews(@PathVariable Long tutorId,
                                              @RequestParam(defaultValue = "5") int limit) {
    List<Review> base = reviewRepo.findTop5ByTutor_IdOrderByCreatedAtDesc(tutorId);
    return base.stream().limit(Math.max(1, limit)).map(ReviewSummaryDto::from).toList();
  }

  @GetMapping("/metrics")
  public ResponseEntity<MetricsDto> metrics(@PathVariable Long tutorId) {
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    OffsetDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

    // Earnings: sum payments captured this month for this tutor
    int earningsCents = paymentRepo
        .findByLesson_Tutor_IdAndStatusAndCreatedAtBetween(tutorId, PaymentStatus.CAPTURED, startOfMonth, endOfMonth)
        .stream()
        .mapToInt(p -> p.getAmountCents() == null ? 0 : p.getAmountCents())
        .sum();

    // CTR: accepted / total booking requests
    long totalReq = bookingRepo.countByTutor_Id(tutorId);
    long accepted = bookingRepo.countByTutor_IdAndStatus(tutorId, BookingStatus.ACCEPTED);
    double ctr = (totalReq == 0) ? 0.0 : ((double) accepted / (double) totalReq);

    // Rating
    TutorProfile tp = tutorProfileRepo.findById(tutorId).orElse(null);
    double ratingAvg = tp != null && tp.getRatingAvg() != null ? tp.getRatingAvg() : 0.0;
    int ratingCount = tp != null && tp.getRatingCount() != null ? tp.getRatingCount() : 0;

    // Lessons this month (all statuses) and number of students
    List<Lesson> monthLessons = lessonRepo.findByTutor_IdAndStartUtcBetween(tutorId, startOfMonth, endOfMonth);
    int lessonsThisMonth = monthLessons.size();
    long numStudents = lessonRepo.countDistinctStudentsByTutorId(tutorId);

    // Average lesson minutes across all lessons
    List<Lesson> allLessons = lessonRepo.findByTutor_Id(tutorId);
    double avgMin = 0.0;
    if (!allLessons.isEmpty()) {
      double sum = 0.0; int n = 0;
      for (Lesson l : allLessons) {
        if (l.getStartUtc() != null && l.getEndUtc() != null) {
          long m = Duration.between(l.getStartUtc(), l.getEndUtc()).toMinutes();
          if (m > 0) { sum += m; n++; }
        }
      }
      avgMin = (n == 0) ? 0.0 : (sum / n);
    }

    MetricsDto dto = MetricsDto.builder()
        .monthlyEarningsCents(earningsCents)
        .clickThroughRate(ctr)
        .overallRatingAvg(ratingAvg)
        .overallRatingCount(ratingCount)
        .numberOfStudents((int) numStudents)
        .lessonsThisMonth(lessonsThisMonth)
        .averageLessonMinutes(avgMin)
        .build();
    return ResponseEntity.ok(dto);
  }

  @Builder
  public static class MetricsDto {
    public Integer monthlyEarningsCents;
    public Double clickThroughRate; // 0..1
    public Double overallRatingAvg;
    public Integer overallRatingCount;
    public Integer numberOfStudents;
    public Integer lessonsThisMonth;
    public Double averageLessonMinutes;
  }

  @Builder
  public static class UpcomingLessonDto {
    public Long id;
    public String studentName;
    public String instrumentName;
    public OffsetDateTime startUtc;
    public Integer durationMin;

    public static UpcomingLessonDto from(Lesson l){
      Integer dur = null;
      if (l.getStartUtc()!=null && l.getEndUtc()!=null){
        dur = (int) java.time.Duration.between(l.getStartUtc(), l.getEndUtc()).toMinutes();
      }
      return UpcomingLessonDto.builder()
          .id(l.getId())
          .studentName(l.getStudent()!=null? l.getStudent().getName(): null)
          .instrumentName(l.getBookingRequest()!=null && l.getBookingRequest().getInstrument()!=null ? l.getBookingRequest().getInstrument().getName() : null)
          .startUtc(l.getStartUtc())
          .durationMin(dur)
          .build();
    }
  }

  @Builder
  public static class ReviewSummaryDto {
    public Long id;
    public String studentName;
    public Integer rating;
    public String text;
    public OffsetDateTime createdAt;

    public static ReviewSummaryDto from(Review r){
      return ReviewSummaryDto.builder()
          .id(r.getId())
          .studentName(r.getReviewerStudent()!=null? r.getReviewerStudent().getName(): null)
          .rating(r.getRating())
          .text(r.getText())
          .createdAt(r.getCreatedAt())
          .build();
    }
  }
}
