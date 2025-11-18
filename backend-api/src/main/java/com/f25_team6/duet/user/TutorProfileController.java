package com.f25_team6.duet.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.CrossOrigin;
import static org.springframework.http.HttpStatus.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/tutor-profiles")
@RequiredArgsConstructor
public class TutorProfileController {

  private final TutorProfileRepository repo;
  private final UserRepository userRepo;

  @PostMapping("/{userId}")
  public ResponseEntity<TutorProfile> create(@PathVariable Long userId, @RequestBody TutorProfile in) {
    User u = userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    if (repo.existsById(userId)) throw new ResponseStatusException(CONFLICT, "Profile exists");
    if (in.getHourlyRateCents() == null) throw new ResponseStatusException(BAD_REQUEST, "hourlyRateCents required");
    in.setUser(u); in.setUserId(userId);
    return ResponseEntity.ok(repo.save(in));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<TutorProfile> get(@PathVariable Long userId) {
    return repo.findById(userId).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{userId}")
  public ResponseEntity<TutorProfile> update(@PathVariable Long userId, @RequestBody TutorProfile in) {
    TutorProfile cur = repo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    if (in.getBio() != null) cur.setBio(in.getBio());
    if (in.getPhotoUrl() != null) cur.setPhotoUrl(in.getPhotoUrl());
    if (in.getHourlyRateCents() != null) cur.setHourlyRateCents(in.getHourlyRateCents());
    cur.setOnlineEnabled(in.isOnlineEnabled());
    cur.setInPersonEnabled(in.isInPersonEnabled());
    if (in.getLatitude() != null) cur.setLatitude(in.getLatitude());
    if (in.getLongitude() != null) cur.setLongitude(in.getLongitude());
    if (in.getCity() != null) cur.setCity(in.getCity());
    if (in.getState() != null) cur.setState(in.getState());
    if (in.getTimezone() != null) cur.setTimezone(in.getTimezone());
    if (in.getCancellationNote() != null) cur.setCancellationNote(in.getCancellationNote());
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable Long userId) {
    if (!repo.existsById(userId)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
