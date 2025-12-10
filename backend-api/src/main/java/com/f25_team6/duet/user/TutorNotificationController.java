package com.f25_team6.duet.user;

import com.f25_team6.duet.booking.*;
import com.f25_team6.duet.messaging.Message;
import com.f25_team6.duet.messaging.MessageRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tutors/{tutorId}/notifications")
@RequiredArgsConstructor
public class TutorNotificationController {

  private final MessageRepository messageRepo;
  private final LessonRepository lessonRepo;
  private final PaymentRepository paymentRepo;
  private final ReviewRepository reviewRepo;
  private final NotificationStateRepository notifStateRepo;
  private final NotificationReadRepository notifReadRepo;

  @GetMapping
  public List<NotificationDto> list(@PathVariable Long tutorId,
                                    @RequestParam(defaultValue = "20") int limit) {
    List<NotificationDto> items = new ArrayList<>();
    var state = notifStateRepo.findById(tutorId).orElse(null);
    var lastAll = state != null ? state.getLastReadAllAt() : null;

    for (Message m : messageRepo.findTop50ByConversation_Tutor_IdOrderByCreatedAtDesc(tutorId)) {
      if (m.getSender() != null && m.getSender().getId().equals(tutorId)) continue; // only incoming
      var n = NotificationDto.message(m.getId(), m.getSender()!=null? m.getSender().getName(): "Student", m.getBody(), m.getCreatedAt());
      n.read = isRead(tutorId, "message", m.getId(), lastAll, m.getCreatedAt());
      items.add(n);
    }

    for (Lesson l : lessonRepo.findByTutor_Id(tutorId)) {
      var n = NotificationDto.lesson(l.getId(), "Lesson scheduled", l.getStartUtc(), l.getCreatedAt());
      n.read = isRead(tutorId, "lesson", l.getId(), lastAll, l.getCreatedAt());
      items.add(n);
    }

    for (Payment p : paymentRepo.findTop50ByLesson_Tutor_IdOrderByCreatedAtDesc(tutorId)) {
      var n = NotificationDto.payment(p.getId(), p.getAmountCents(), p.getCreatedAt());
      n.read = isRead(tutorId, "payment", p.getId(), lastAll, p.getCreatedAt());
      items.add(n);
    }

    for (Review r : reviewRepo.findTop5ByTutor_IdOrderByCreatedAtDesc(tutorId)) {
      var n = NotificationDto.review(r.getId(), r.getReviewerStudent()!=null? r.getReviewerStudent().getName(): "Student", r.getRating(), r.getCreatedAt());
      n.read = isRead(tutorId, "review", r.getId(), lastAll, r.getCreatedAt());
      items.add(n);
    }

    items.sort(Comparator.comparing((NotificationDto n) -> n.createdAt).reversed());
    return items.subList(0, Math.min(limit, items.size()));
  }

  private boolean isRead(Long tutorId, String type, Long itemId, OffsetDateTime lastAll, OffsetDateTime createdAt){
    if (lastAll != null && createdAt != null && !createdAt.isAfter(lastAll)) return true;
    return notifReadRepo.existsByTutorUserIdAndTypeAndItemId(tutorId, type, itemId);
  }

  @GetMapping("/unread-count")
  public long unreadCount(@PathVariable Long tutorId){
    var items = list(tutorId, 1000);
    return items.stream().filter(n -> n.read == null || !n.read).count();
  }

  @PutMapping("/read-all")
  public void readAll(@PathVariable Long tutorId){
    var state = notifStateRepo.findById(tutorId).orElse(NotificationState.builder().tutorUserId(tutorId).build());
    state.setLastReadAllAt(OffsetDateTime.now());
    notifStateRepo.save(state);
  }

  @PutMapping("/read-item")
  public void readItem(@PathVariable Long tutorId, @RequestBody ReadReq req){
    if (req == null || req.type == null || req.itemId == null) return;
    if (!notifReadRepo.existsByTutorUserIdAndTypeAndItemId(tutorId, req.type, req.itemId)){
      notifReadRepo.save(NotificationRead.builder().tutorUserId(tutorId).type(req.type).itemId(req.itemId).build());
    }
  }

  @Builder
  public static class NotificationDto {
    public String type; // message|lesson|payment|review
    public Long id;
    public String title;
    public String body;
    public OffsetDateTime when;
    public OffsetDateTime createdAt;
    public Boolean read;

    public static NotificationDto message(Long id, String from, String text, OffsetDateTime createdAt){
      return NotificationDto.builder()
          .type("message").id(id).title("New message")
          .body(from + ": " + (text!=null? text: ""))
          .createdAt(createdAt).when(createdAt)
          .build();
    }
    public static NotificationDto lesson(Long id, String title, OffsetDateTime when, OffsetDateTime createdAt){
      return NotificationDto.builder()
          .type("lesson").id(id).title(title)
          .when(when).createdAt(createdAt)
          .build();
    }
    public static NotificationDto payment(Long id, Integer amountCents, OffsetDateTime createdAt){
      return NotificationDto.builder()
          .type("payment").id(id).title("Payment received")
          .body(amountCents != null ? ("$" + (amountCents/100.0)) : null)
          .createdAt(createdAt).when(createdAt)
          .build();
    }
    public static NotificationDto review(Long id, String from, Integer rating, OffsetDateTime createdAt){
      return NotificationDto.builder()
          .type("review").id(id).title("New review")
          .body(from + " — " + (rating!=null? rating: 0) + "/5")
          .createdAt(createdAt).when(createdAt)
          .build();
    }
  }

  public static class ReadReq { public String type; public Long itemId; }
}
