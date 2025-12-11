package com.f25_team6.duet.admin;

import com.f25_team6.duet.booking.BookingRequestRepository;
import com.f25_team6.duet.booking.LessonRepository;
import com.f25_team6.duet.booking.PaymentRepository;
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
    private final ReviewRepository reviewRepo;
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final LessonRepository lessonRepo;
    private final PaymentRepository paymentRepo;

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
    public ResponseEntity<List<User>> generateTutors(@RequestParam int count) {
        List<User> users = generateUsers(count, UserRole.TUTOR);
        // Create profiles for tutors
        Random rand = new Random();
        for (User u : users) {
            TutorProfile p = TutorProfile.builder()
                    .user(u)
                    .bio("Randomly generated tutor bio. Teaching is my passion!")
                    .hourlyRateCents(1000 + rand.nextInt(9000)) // $10 - $100
                    .onlineEnabled(rand.nextBoolean())
                    .inPersonEnabled(rand.nextBoolean())
                    .city("Greensboro")
                    .state("NC")
                    .timezone("America/New_York")
                    .build();
            // Ensure at least one mode is enabled
            if (!p.isOnlineEnabled() && !p.isInPersonEnabled()) {
                p.setOnlineEnabled(true);
            }
            tutorProfileRepo.save(p);
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
}
