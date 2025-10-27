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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageRepository repo;
  private final ConversationRepository convRepo;
  private final UserRepository userRepo;

  @PostMapping
  public ResponseEntity<Message> create(@RequestBody CreateReq req) {
    Conversation conv = convRepo.findById(req.conversationId())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Conversation not found"));
    User sender = userRepo.findById(req.senderUserId())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Sender not found"));
    Message m = Message.builder().conversation(conv).sender(sender).body(req.body()).build();
    return ResponseEntity.ok(repo.save(m));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Message> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<Message> list() { return repo.findAll(); }

  @GetMapping("/by-conversation/{conversationId}")
  public List<Message> byConversation(@PathVariable Long conversationId) {
    return repo.findByConversationIdOrderByCreatedAtAsc(conversationId);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Message> update(@PathVariable Long id, @RequestBody UpdateReq req) {
    Message cur = repo.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    if (req.body() != null) cur.setBody(req.body());
    if (req.isAbusiveFlag() != null) cur.setAbusiveFlag(req.isAbusiveFlag());
    return ResponseEntity.ok(cur);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) throw new ResponseStatusException(NOT_FOUND);
    repo.deleteById(id); return ResponseEntity.noContent().build();
  }

  public record CreateReq(Long conversationId, Long senderUserId, String body) {}
  public record UpdateReq(String body, Boolean isAbusiveFlag) {}
}
