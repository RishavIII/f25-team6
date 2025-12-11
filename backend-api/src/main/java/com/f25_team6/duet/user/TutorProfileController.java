package com.f25_team6.duet.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.http.HttpStatus.*;
import org.springframework.http.MediaType;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "false")
@RequestMapping("/api/tutor-profiles")
@RequiredArgsConstructor
public class TutorProfileController {

  private final TutorProfileRepository repo;
  private final UserRepository userRepo;

  @PostMapping("/{userId}")
  public ResponseEntity<TutorProfile> create(@PathVariable Long userId, @RequestBody TutorProfile in) {
    User user = userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

    if (repo.countByUserId(userId) > 0)
      throw new ResponseStatusException(CONFLICT, "Profile exists");

    if (in.getHourlyRateCents() == null)
      throw new ResponseStatusException(BAD_REQUEST, "hourlyRateCents required");

    in.setUser(user);
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
    if (in.getBio() != null)
      cur.setBio(in.getBio());
    if (in.getPhotoUrl() != null)
      cur.setPhotoUrl(in.getPhotoUrl());
    if (in.getHourlyRateCents() != null)
      cur.setHourlyRateCents(in.getHourlyRateCents());
    cur.setOnlineEnabled(in.isOnlineEnabled());
    cur.setInPersonEnabled(in.isInPersonEnabled());
    if (in.getLatitude() != null)
      cur.setLatitude(in.getLatitude());
    if (in.getLongitude() != null)
      cur.setLongitude(in.getLongitude());
    if (in.getCity() != null)
      cur.setCity(in.getCity());
    if (in.getState() != null)
      cur.setState(in.getState());
    if (in.getTimezone() != null)
      cur.setTimezone(in.getTimezone());
    if (in.getCancellationNote() != null)
      cur.setCancellationNote(in.getCancellationNote());
    repo.save(cur);
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable Long userId) {
    if (!repo.existsById(userId))
      throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{userId}/photo")
  public ResponseEntity<Map<String, String>> uploadPhoto(@PathVariable Long userId,
      @RequestParam("file") MultipartFile file) throws IOException {

    TutorProfile profile = repo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

    profile.setPhotoBlob(file.getBytes());
    profile.setPhotoContentType(file.getContentType());

    String photoUrl = "/api/tutor-profiles/" + userId + "/photo";
    profile.setPhotoUrl(photoUrl);

    repo.save(profile);

    Map<String, String> response = new HashMap<>();
    response.put("photoUrl", photoUrl);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/photo")
  public ResponseEntity<byte[]> getPhoto(@PathVariable Long userId) {
    TutorProfile profile = repo.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

    if (profile.getPhotoBlob() == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .contentType(MediaType
            .parseMediaType(profile.getPhotoContentType() != null ? profile.getPhotoContentType() : "image/jpeg"))
        .body(profile.getPhotoBlob());
  }
}
