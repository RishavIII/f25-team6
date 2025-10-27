package com.f25_team6.duet.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

  private final InstrumentRepository repo;

  @PostMapping
  public ResponseEntity<Instrument> create(@RequestBody Instrument in) {
    if (in.getName() == null || in.getName().isBlank())
      throw new ResponseStatusException(BAD_REQUEST, "name required");
    return ResponseEntity.ok(repo.save(in));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Instrument> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<Instrument> list() { return repo.findAll(); }

  @PutMapping("/{id}")
  public ResponseEntity<Instrument> update(@PathVariable Long id, @RequestBody Instrument in) {
    Instrument cur = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    if (in.getName() != null) cur.setName(in.getName());
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
