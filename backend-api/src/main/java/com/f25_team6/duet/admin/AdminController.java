package com.f25_team6.duet.admin;

import com.f25_team6.duet.booking.BookingRequestRepository;
import com.f25_team6.duet.booking.LessonRepository;
import com.f25_team6.duet.booking.PaymentRepository;
import com.f25_team6.duet.messaging.Conversation;
import com.f25_team6.duet.messaging.Message;
import java.util.List;
import com.f25_team6.duet.booking.ReviewRepository;
import com.f25_team6.duet.common.enums.UserRole;
import com.f25_team6.duet.messaging.Conversation;
import com.f25_team6.duet.messaging.ConversationRepository;
import com.f25_team6.duet.messaging.MessageRepository;
import com.f25_team6.duet.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AdminController {

    private final UserRepository userRepo;
    private final TutorProfileRepository tutorProfileRepo;
    private final BookingRequestRepository bookingRequestRepo;
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final LessonRepository lessonRepo;
    private final PaymentRepository paymentRepo;
    private final ReviewRepository reviewRepo;
    private final com.f25_team6.duet.catalog.InstrumentRepository instrumentRepo;
    private final com.f25_team6.duet.booking.BookingService bookingService;
    private final com.f25_team6.duet.booking.TutorRatingService tutorRatingService;

    @GetMapping("/users")
    public List<User> listUsers() {
        return userRepo.findAll();
    }

    @Transactional
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "User not found");
        }

        // 1. Delete Dependencies Manually

        // Reviews
        reviewRepo.deleteByTutor_Id(id);
        reviewRepo.deleteByReviewerStudent_Id(id);

        // Payments (referencing Lessons)
        paymentRepo.deleteByLesson_Tutor_Id(id);
        paymentRepo.deleteByStudent_Id(id);

        // Lessons (referencing Bookings and Users)
        lessonRepo.deleteByTutor_Id(id);
        lessonRepo.deleteByStudent_Id(id);

        // Bookings
        bookingRequestRepo.deleteByTutor_Id(id);
        bookingRequestRepo.deleteByStudent_Id(id);

        // Messages & Conversations: Conversations must be cleared of messages first
        List<Conversation> convos = conversationRepo.findByStudent_Id(id);
        convos.addAll(conversationRepo.findByTutor_Id(id));

        // Use a set to avoid duplicates if user is both? (Conversation has unique
        // constraint, so list might have dupes if logic fetch same convo?)
        // `findByStudent_Id` and `findByTutor_Id` are distinct sets since a user is
        // either student OR tutor in a convo usually,
        // but technically User entity doesn't enforce role per relationship strictly in
        // code, but app logic does.
        // Safer to just iterate.
        for (Conversation c : convos) {
            messageRepo.deleteByConversationId(c.getId());
            conversationRepo.delete(c);
        }

        // Tutor Profile
        Optional<TutorProfile> profile = tutorProfileRepo.findById(id);
        profile.ifPresent(tutorProfileRepo::delete);

        // User
        userRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteAllUsers() {
        // Delete dependencies first (order matters for FKs)
        paymentRepo.deleteAll();
        reviewRepo.deleteAll();
        // Lessons reference Bookings, so delete Lessons first usually?
        // Lesson references BookingRequest (OneToOne). If Lesson exists, BookingRequest
        // cannot be deleted if FK constraint
        lessonRepo.deleteAll();

        messageRepo.deleteAll();
        conversationRepo.deleteAll();
        bookingRequestRepo.deleteAll();
        tutorProfileRepo.deleteAll();
        userRepo.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate/students")
    public ResponseEntity<List<User>> generateStudents(@RequestParam int count) {
        return ResponseEntity.ok(generateUsers(count, UserRole.STUDENT));
    }

    @PostMapping("/generate/tutors")
    @Transactional
    public ResponseEntity<List<User>> generateTutors(@RequestParam int count) {
        System.out.println("Generating " + count + " tutors...");
        List<User> users = generateUsers(count, UserRole.TUTOR);

        // Fetch available instruments
        List<com.f25_team6.duet.catalog.Instrument> instruments = instrumentRepo.findAll();
        if (instruments.isEmpty()) {
            // Log warning or handle? Assuming instruments exist from seed.
            System.err.println("No instruments found for tutor generation.");
        }

        Random rand = new Random();
        for (User u : users) {
            System.out.println("Creating profile for tutor user " + u.getId());
            TutorProfile p = TutorProfile.builder()
                    .user(u)
                    .bio("Randomly generated tutor bio. Teaching is my passion!")
                    .hourlyRateCents(1000 + rand.nextInt(9000)) // $10 - $100
                    .onlineEnabled(rand.nextBoolean())
                    .inPersonEnabled(rand.nextBoolean())
                    .city("Greensboro")
                    .state("NC")
                    .zipcode("2741" + rand.nextInt(3)) // 27410, 27411, 27412
                    .timezone("America/New_York")
                    .build();
            // Ensure at least one mode is enabled
            if (!p.isOnlineEnabled() && !p.isInPersonEnabled()) {
                p.setOnlineEnabled(true);
            }
            // Save profile first
            p = tutorProfileRepo.save(p);

            // Add Instruments (simulate onboarding)
            if (!instruments.isEmpty()) {
                int numInstruments = 1 + rand.nextInt(3); // 1 to 3 instruments
                for (int j = 0; j < numInstruments; j++) {
                    com.f25_team6.duet.catalog.Instrument instr = instruments.get(rand.nextInt(instruments.size()));
                    // Avoid duplicates for same tutor?
                    // Simple check:
                    boolean exists = p.getInstruments().stream()
                            .anyMatch(ti -> ti.getInstrument().getId().equals(instr.getId()));
                    if (exists)
                        continue;

                    com.f25_team6.duet.catalog.TutorInstrument ti = com.f25_team6.duet.catalog.TutorInstrument.builder()
                            .tutor(p)
                            .instrument(instr)
                            .minLevel(com.f25_team6.duet.common.enums.Level.BEGINNER)
                            .maxLevel(com.f25_team6.duet.common.enums.Level.EXPERT)
                            .build();
                    p.getInstruments().add(ti);
                }
                tutorProfileRepo.save(p); // Cascade update instruments
            }

            System.out.println(
                    "Saved profile for tutor " + u.getId() + " with " + p.getInstruments().size() + " instruments.");
        }
        return ResponseEntity.ok(users);
    }

    private List<User> generateUsers(int count, UserRole role) {
        Random rand = new Random();
        List<String> firstNames = List.of("James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
                "William", "Elizabeth");
        List<String> lastNames = List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez");

        java.util.List<User> newUsers = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            String first = firstNames.get(rand.nextInt(firstNames.size()));
            String last = lastNames.get(rand.nextInt(lastNames.size()));
            String name = first + " " + last;
            String email = first.toLowerCase() + "." + last.toLowerCase() + "."
                    + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

            User u = User.builder()
                    .name(name)
                    .email(email)
                    .password("password") // Default password
                    .role(role)
                    .phone("555-01" + String.format("%02d", rand.nextInt(100)))
                    .build();
            userRepo.save(u);
            newUsers.add(u);
        }
        return newUsers;
    }

    @PostMapping("/messages")
    public ResponseEntity<Void> sendMessages(@RequestBody AdminMessageRequest req) {
        if (req.fromUserIds == null || req.toUserIds == null || req.body == null) {
            return ResponseEntity.badRequest().build();
        }

        for (Long fromId : req.fromUserIds) {
            User sender = userRepo.findById(fromId).orElse(null);
            if (sender == null)
                continue;

            for (Long toId : req.toUserIds) {
                User receiver = userRepo.findById(toId).orElse(null);
                if (receiver == null || receiver.getId().equals(sender.getId()))
                    continue;

                User student = null;
                User tutor = null;

                if (sender.getRole() == UserRole.STUDENT && receiver.getRole() == UserRole.TUTOR) {
                    student = sender;
                    tutor = receiver;
                } else if (sender.getRole() == UserRole.TUTOR && receiver.getRole() == UserRole.STUDENT) {
                    student = receiver;
                    tutor = sender;
                }

                if (student != null && tutor != null) {
                    Conversation c = conversationRepo.findByStudentIdAndTutorId(student.getId(), tutor.getId())
                            .orElse(null);
                    if (c == null) {
                        c = conversationRepo.save(Conversation.builder().student(student).tutor(tutor).build());
                    }

                    Message m = Message.builder()
                            .conversation(c)
                            .sender(sender)
                            .body(req.body)
                            .build();
                    messageRepo.save(m);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bookings")
    public ResponseEntity<Void> createBooking(@RequestBody AdminBookingRequest req) {
        if (req.studentId == null || req.tutorId == null)
            return ResponseEntity.badRequest().build();

        bookingService.createRequest(
                req.studentId,
                req.tutorId,
                req.instrumentId,
                req.level != null ? req.level : com.f25_team6.duet.common.enums.Level.BEGINNER,
                req.durationMin > 0 ? req.durationMin : 60,
                req.requestedStartUtc != null ? req.requestedStartUtc : java.time.OffsetDateTime.now().plusDays(1),
                com.f25_team6.duet.common.enums.LessonMode.ONLINE,
                "Admin simulated request");

        return ResponseEntity.ok().build();
    }

    @lombok.Data
    public static class AdminMessageRequest {
        public List<Long> fromUserIds;
        public List<Long> toUserIds;
        public String body;
    }

    @lombok.Data
    public static class AdminBookingRequest {
        public Long studentId;
        public Long tutorId;
        public Long instrumentId;
        public com.f25_team6.duet.common.enums.Level level;
        public int durationMin;
        public java.time.OffsetDateTime requestedStartUtc;
    }

    @GetMapping("/lessons")
    public List<AdminLessonDto> getCompletedLessons(
            @RequestParam Long studentId,
            @RequestParam Long tutorId) {
        List<com.f25_team6.duet.booking.Lesson> lessons = lessonRepo.findByStudent_IdAndTutor_IdAndStatus(
                studentId, tutorId, com.f25_team6.duet.common.enums.LessonStatus.COMPLETED);
        return lessons.stream().map(l -> {
            AdminLessonDto dto = new AdminLessonDto();
            dto.id = l.getId();
            dto.startUtc = l.getStartUtc();
            dto.instrumentName = l.getBookingRequest() != null && l.getBookingRequest().getInstrument() != null
                    ? l.getBookingRequest().getInstrument().getName()
                    : "Unknown";
            return dto;
        }).toList();
    }

    @lombok.Data
    public static class AdminLessonDto {
        public Long id;
        public java.time.OffsetDateTime startUtc;
        public String instrumentName;
    }

    @PostMapping("/reviews")
    @Transactional
    public ResponseEntity<com.f25_team6.duet.booking.Review> createReview(@RequestBody AdminReviewRequest req) {
        if (req.studentId == null || req.tutorId == null || req.rating == null || req.lessonId == null) {
            return ResponseEntity.badRequest().build();
        }
        if (req.rating < 1 || req.rating > 5) {
            return ResponseEntity.badRequest().build();
        }

        User student = userRepo.findById(req.studentId).orElse(null);
        User tutor = userRepo.findById(req.tutorId).orElse(null);
        com.f25_team6.duet.booking.Lesson lesson = lessonRepo.findById(req.lessonId).orElse(null);
        if (student == null || tutor == null || lesson == null) {
            throw new ResponseStatusException(NOT_FOUND, "Student, Tutor, or Lesson not found");
        }

        com.f25_team6.duet.booking.Review review = com.f25_team6.duet.booking.Review.builder()
                .lesson(lesson)
                .tutor(tutor)
                .reviewerStudent(student)
                .rating(req.rating)
                .text(req.text)
                .build();
        review = reviewRepo.save(review);

        // Update tutor's rating stats
        tutorRatingService.applyNewReview(req.tutorId, req.rating);

        return ResponseEntity.ok(review);
    }

    @lombok.Data
    public static class AdminReviewRequest {
        public Long studentId;
        public Long tutorId;
        public Long lessonId;
        public Integer rating;
        public String text;
    }
}
