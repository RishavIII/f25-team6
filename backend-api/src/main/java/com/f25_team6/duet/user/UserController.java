package com.f25_team6.duet.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository repo;

  @PostMapping
  public ResponseEntity<User> create(@RequestBody User u) {
    if (u.getEmail() == null || u.getEmail().isBlank())
      throw new ResponseStatusException(BAD_REQUEST, "Email required");
    if (repo.existsByEmailIgnoreCase(u.getEmail()))
      throw new ResponseStatusException(CONFLICT, "Email in use");
    return ResponseEntity.ok(repo.save(u));
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<User> list() { return repo.findAll(); }

  @PutMapping("/{id}")
  public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User in) {
    User cur = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    if (in.getEmail() != null && !in.getEmail().equalsIgnoreCase(cur.getEmail())) {
      if (repo.existsByEmailIgnoreCase(in.getEmail()))
        throw new ResponseStatusException(CONFLICT, "Email in use");
      cur.setEmail(in.getEmail());
    }
    if (in.getPassword() != null) cur.setPassword(in.getPassword());
    if (in.getName() != null) cur.setName(in.getName());
    if (in.getPhone() != null) cur.setPhone(in.getPhone());
    if (in.getRole() != null) cur.setRole(in.getRole());
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
