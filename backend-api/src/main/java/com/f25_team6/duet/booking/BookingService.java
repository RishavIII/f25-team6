package com.f25_team6.duet.booking;

import com.f25_team6.duet.common.enums.*;
import com.f25_team6.duet.user.*;
import com.f25_team6.duet.catalog.Instrument;
import com.f25_team6.duet.catalog.InstrumentRepository;
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

    public BookingRequest createRequest(Long studentId, Long tutorId, Long instrumentId, Level level, int durationMin,
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
        return bookingRepo.save(br);
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

        OffsetDateTime start = br.getAltProposedStartUtc() != null ? br.getAltProposedStartUtc()
                : br.getRequestedStartUtc();
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

    public BookingRequest proposeAlternate(Long bookingId, OffsetDateTime altStart) {
        BookingRequest br = getBooking(bookingId);
        if (br.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(CONFLICT, "Only pending can receive a proposal.");
        }
        br.setStatus(BookingStatus.ALT_PROPOSED);
        br.setAltProposedStartUtc(altStart);
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
        if (!lesson.getStudent().getId().equals(studentId)) {
            throw new ResponseStatusException(FORBIDDEN, "Only the student who took the lesson can review it.");
        }
        Review r = Review.builder()
                .lesson(lesson)
                .tutor(lesson.getTutor())
                .reviewerStudent(lesson.getStudent())
                .rating(rating)
                .text(text)
                .build();
        return reviewRepo.save(r);
    }
    private BookingRequest getBooking(Long id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking request not found"));
    }
}