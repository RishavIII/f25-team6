package com.f25_team6.duet.messaging;

import com.f25_team6.duet.user.User;
import com.f25_team6.duet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationRepository repo;
  private final UserRepository userRepo;

  @PostMapping
  public ResponseEntity<Conversation> create(@RequestBody CreateReq req) {
    User student = userRepo.findById(req.studentUserId())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Student not found"));
    User tutor = userRepo.findById(req.tutorUserId())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tutor not found"));

    var existing = repo.findByStudentIdAndTutorId(student.getId(), tutor.getId());
    if (existing.isPresent()) return ResponseEntity.ok(existing.get());

    Conversation c = Conversation.builder().student(student).tutor(tutor).build();
    return ResponseEntity.ok(repo.save(c));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Conversation> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<Conversation> list() { return repo.findAll(); }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  public record CreateReq(Long studentUserId, Long tutorUserId) {}
}
