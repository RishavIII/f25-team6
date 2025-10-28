package com.f25_team6.duet.catalog;

import com.f25_team6.duet.user.TutorProfileRepository;
import com.f25_team6.duet.user.TutorProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/tutor-instruments")
@RequiredArgsConstructor
public class TutorInstrumentController {

  private final TutorInstrumentRepository repo;
  private final TutorProfileRepository tutorRepo;
  private final InstrumentRepository instrumentRepo;

  @PostMapping
  public ResponseEntity<TutorInstrument> create(@RequestBody CreateReq req) {
    TutorProfile tutor = tutorRepo.findById(req.tutorUserId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tutor profile not found"));
    Instrument inst = instrumentRepo.findById(req.instrumentId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Instrument not found"));
    TutorInstrument ti = TutorInstrument.builder()
        .tutor(tutor).instrument(inst)
        .minLevel(req.minLevel).maxLevel(req.maxLevel)
        .build();
    return ResponseEntity.ok(repo.save(ti));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TutorInstrument> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<TutorInstrument> list() { return repo.findAll(); }

  @PutMapping("/{id}")
  public ResponseEntity<TutorInstrument> update(@PathVariable Long id, @RequestBody UpdateReq req) {
    TutorInstrument cur = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    if (req.minLevel != null) cur.setMinLevel(req.minLevel);
    if (req.maxLevel != null) cur.setMaxLevel(req.maxLevel);
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(id); return ResponseEntity.noContent().build();
  }

  // DTOs
  public static record CreateReq(Long tutorUserId, Long instrumentId,
                                 com.f25_team6.duet.common.enums.Level minLevel,
                                 com.f25_team6.duet.common.enums.Level maxLevel) {}
  public static record UpdateReq(com.f25_team6.duet.common.enums.Level minLevel,
                                 com.f25_team6.duet.common.enums.Level maxLevel) {}
}
