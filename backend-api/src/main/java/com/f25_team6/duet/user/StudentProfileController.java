package com.f25_team6.duet.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/student-profiles")
@RequiredArgsConstructor
public class StudentProfileController {

  private final StudentProfileRepository repo;
  private final UserRepository userRepo;

  @PostMapping("/{userId}")
  public ResponseEntity<StudentProfile> create(@PathVariable Long userId, @RequestBody StudentProfile in) {
    User u = userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    if (repo.existsById(userId)) throw new ResponseStatusException(CONFLICT, "Profile exists");
    in.setUser(u); in.setUserId(userId);
    return ResponseEntity.ok(repo.save(in));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<StudentProfile> get(@PathVariable Long userId) {
    return repo.findById(userId).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{userId}")
  public ResponseEntity<StudentProfile> update(@PathVariable Long userId, @RequestBody StudentProfile in) {
    StudentProfile cur = repo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    if (in.getContactPreferences() != null) cur.setContactPreferences(in.getContactPreferences());
    cur.setPrefersOnline(in.isPrefersOnline());
    cur.setPrefersInPerson(in.isPrefersInPerson());
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable Long userId) {
    if (!repo.existsById(userId)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
