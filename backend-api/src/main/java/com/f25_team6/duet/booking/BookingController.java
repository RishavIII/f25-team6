package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.LessonMode;
import com.f25_team6.duet.common.enums.Level;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRequestRepository bookingRepo;
    private final LessonRepository lessonRepo;
    private final BookingService bookingService;

    @PostMapping("/booking-requests")
    public ResponseEntity<BookingRequest> create(@RequestBody CreateBookingRequest req) {
        return ResponseEntity.ok(bookingService.createRequest(
                req.studentId, req.tutorId, req.instrumentId, req.level, req.durationMin,
                req.requestedStartUtc, req.lessonMode, req.notes));
    }

    @GetMapping("/booking-requests/{id}")
    public ResponseEntity<BookingRequest> get(@PathVariable Long id) {
        return bookingRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/booking-requests")
    public List<BookingRequest> list() {
        return bookingRepo.findAll();
    }

    @PostMapping("/booking-requests/{id}/accept")
    public ResponseEntity<BookingRequest> accept(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.accept(id));
    }

    @PostMapping("/booking-requests/{id}/decline")
    public ResponseEntity<BookingRequest> decline(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.decline(id));
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<Lesson> getLesson(@PathVariable Long id) {
        return lessonRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lessons")
    public List<Lesson> listLessons() {
        return lessonRepo.findAll();
    }

    @PostMapping("/lessons/{lessonId}/pay")
    public ResponseEntity<PaymentSummary> pay(@PathVariable Long lessonId,
            @RequestParam int amountCents) {
        Payment p = bookingService.pay(lessonId, amountCents);
        return ResponseEntity.ok(PaymentSummary.from(p));
    }

    @PostMapping("/lessons/{lessonId}/review")
    public ResponseEntity<Review> reviewLesson(@PathVariable Long lessonId, @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(bookingService.review(
                lessonId, req.studentId, req.rating, req.text));
    }

    @Data
    public static class CreateBookingRequest {
        public Long studentId;
        public Long tutorId;
        public Long instrumentId;
        public Level level;
        public int durationMin;
        public OffsetDateTime requestedStartUtc;
        public LessonMode lessonMode;
        public String notes;
    }

    @Data
    public static class ReviewRequest {
        public Long studentId;
        public Integer rating;
        public String text;
    }

    public record PaymentSummary(
            Long id,
            Long lessonId,
            Long studentId,
            Integer amountCents,
            com.f25_team6.duet.common.enums.PaymentStatus status,
            String processorRef,
            java.time.OffsetDateTime createdAt) {
        public static PaymentSummary from(Payment p) {
            return new PaymentSummary(
                    p.getId(),
                    p.getLesson().getId(),
                    p.getStudent().getId(),
                    p.getAmountCents(),
                    p.getStatus(),
                    p.getProcessorRef(),
                    p.getCreatedAt());
        }
    }
}
