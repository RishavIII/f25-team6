package com.f25_team6.duet.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorSearchController {

  private final TutorProfileRepository repo;

  // /api/tutors/search?instrumentId=1&online=true&inPerson=false&maxRate=6000
  @GetMapping("/search")
  public List<TutorProfile> search(@RequestParam(required=false) Long instrumentId,
                                   @RequestParam(required=false) Boolean online,
                                   @RequestParam(required=false) Boolean inPerson,
                                   @RequestParam(required=false) Integer maxRate) {
    return repo.search(instrumentId, online, inPerson, maxRate);
  }
}
