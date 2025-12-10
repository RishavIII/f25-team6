package com.f25_team6.duet.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Minimal authentication endpoints for the prototype.
 * NOTE: passwords are stored in plaintext in the existing model; this is
 * intentionally simple for the exercise. Do NOT use in production.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

  private final UserRepository userRepo;

  public static class LoginReq { public String email; public String password; }

  @PostMapping("/login")
  public ResponseEntity<User> login(@RequestBody LoginReq req) {
    if (req == null || req.email == null || req.password == null)
      throw new ResponseStatusException(BAD_REQUEST, "email and password required");
    return userRepo.findByEmailIgnoreCase(req.email)
      .map(u -> {
        if (u.getPassword().equals(req.password)) return ResponseEntity.ok(u);
        throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
      })
      .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));
  }
}
