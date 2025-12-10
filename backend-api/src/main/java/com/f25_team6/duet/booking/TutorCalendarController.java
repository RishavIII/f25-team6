package com.f25_team6.duet.booking;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "false")
@RequestMapping("/api/tutors/{tutorId}/calendar")
@RequiredArgsConstructor
public class TutorCalendarController {

  private final LessonRepository lessonRepo;

  @GetMapping
  public List<CalendarEvent> list(@PathVariable Long tutorId,
                                  @RequestParam int year,
                                  @RequestParam int month) { // 1-12
    OffsetDateTime start = OffsetDateTime.now().withYear(year).withMonth(month).withDayOfMonth(1)
        .withHour(0).withMinute(0).withSecond(0).withNano(0);
    OffsetDateTime end = start.plusMonths(1).minusNanos(1);
    return lessonRepo.findByTutor_IdAndStartUtcBetween(tutorId, start, end)
        .stream().map(CalendarEvent::from).toList();
  }

  @Builder
  public static class CalendarEvent {
    public Long id;
    public String studentName;
    public String mode;
    public String status;
    public OffsetDateTime startUtc;
    public OffsetDateTime endUtc;

    public static CalendarEvent from(Lesson l){
      return CalendarEvent.builder()
          .id(l.getId())
          .studentName(l.getStudent()!=null? l.getStudent().getName(): null)
          .mode(l.getLessonMode()!=null? l.getLessonMode().name(): null)
          .status(l.getStatus()!=null? l.getStatus().name(): null)
          .startUtc(l.getStartUtc())
          .endUtc(l.getEndUtc())
          .build();
    }
  }
}
