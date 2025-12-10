package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.*;
import com.f25_team6.duet.user.*;
import com.f25_team6.duet.catalog.Instrument;
import com.f25_team6.duet.catalog.InstrumentRepository;
import com.f25_team6.duet.messaging.Conversation;
import com.f25_team6.duet.messaging.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
        private final BookingRequestRepository bookingRepo;
        private final LessonRepository lessonRepo;
        private final PaymentRepository paymentRepo;
        private final ReviewRepository reviewRepo;
        private final UserRepository userRepo;
        private final TutorProfileRepository tutorProfileRepo;
        private final InstrumentRepository instrumentRepo;
        private final TutorRatingService tutorRatingService;
        private final ConversationRepository conversationRepo;

        public BookingRequest createRequest(Long studentId, Long tutorId, Long instrumentId, Level level,
                        int durationMin,
                        OffsetDateTime requestStartUtc, LessonMode mode, String notes) {
                User student = userRepo.findById(studentId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Student user not found"));
                User tutor = userRepo.findById(tutorId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tutor user not found"));
                Instrument instrument = instrumentRepo.findById(instrumentId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Instrument not found"));

                BookingRequest br = BookingRequest.builder()
                                .student(student)
                                .tutor(tutor)
                                .instrument(instrument)
                                .level(level)
                                .durationMin(durationMin)
                                .requestedStartUtc(requestStartUtc)
                                .lessonMode(mode)
                                .notes(notes)
                                .status(BookingStatus.PENDING)
                                .build();
                br = bookingRepo.save(br);
                // Ensure a conversation exists between student and tutor upon request creation
                conversationRepo.findByStudentIdAndTutorId(student.getId(), tutor.getId())
                        .or(() -> {
                                Conversation c = Conversation.builder().student(student).tutor(tutor).build();
                                return java.util.Optional.of(conversationRepo.save(c));
                        });
                return br;
        }

        public BookingRequest accept(Long bookingId) {
                BookingRequest br = getBooking(bookingId);
                if (br.getStatus() != BookingStatus.PENDING && br.getStatus() != BookingStatus.ALT_PROPOSED) {
                        throw new ResponseStatusException(CONFLICT, "Cannot accept from current state.");
                }
                br.setStatus(BookingStatus.ACCEPTED);
                br.setUpdatedAt(OffsetDateTime.now());

                TutorProfile tutorProfile = tutorProfileRepo.findById(br.getTutor().getId())
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tutor profile not found"));

                OffsetDateTime start = br.getRequestedStartUtc();
                OffsetDateTime end = start.plusMinutes(br.getDurationMin());

                Lesson lesson = Lesson.builder()
                                .bookingRequest(br)
                                .tutor(br.getTutor())
                                .student(br.getStudent())
                                .startUtc(start)
                                .endUtc(end)
                                .lessonMode(br.getLessonMode())
                                .priceCents(tutorProfile.getHourlyRateCents() * br.getDurationMin() / 60)
                                .status(LessonStatus.SCHEDULED)
                                .build();
                lessonRepo.save(lesson);

                return br;
        }

        public BookingRequest decline(Long bookingId) {
                BookingRequest br = getBooking(bookingId);
                if (br.getStatus() != BookingStatus.PENDING && br.getStatus() != BookingStatus.ALT_PROPOSED) {
                        throw new ResponseStatusException(CONFLICT, "Cannot decline from current state.");
                }
                br.setStatus(BookingStatus.DECLINED);
                br.setUpdatedAt(OffsetDateTime.now());
                return br;
        }

        public Payment pay(Long lessonId, int amountCents) {
                Lesson lesson = lessonRepo.findById(lessonId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Lesson not found"));
                Payment p = Payment.builder()
                                .lesson(lesson)
                                .student(lesson.getStudent())
                                .amountCents(amountCents)
                                .status(PaymentStatus.CAPTURED)
                                .processorRef(lessonId.toString())
                                .build();
                return paymentRepo.save(p);
        }

        public Review review(Long lessonId, Long studentId, int rating, String text) {
                Lesson lesson = lessonRepo.findById(lessonId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Lesson not found"));
                if (!lesson.getStudent().getId().equals(studentId))
                        throw new ResponseStatusException(FORBIDDEN, "Only the student can review this lesson.");

                Review r = Review.builder()
                                .lesson(lesson)
                                .tutor(lesson.getTutor())
                                .reviewerStudent(lesson.getStudent())
                                .rating(rating)
                                .text(text)
                                .build();
                r = reviewRepo.save(r);

                tutorRatingService.applyNewReview(lesson.getTutor().getId(), rating);

                return r;
        }

        private BookingRequest getBooking(Long id) {
                return bookingRepo.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking request not found"));
        }
}