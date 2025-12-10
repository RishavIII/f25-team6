package com.f25_team6.duet.messaging;

import com.f25_team6.duet.user.User;
import com.f25_team6.duet.user.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "false")
@RequestMapping("/api/tutors/{tutorId}")
@RequiredArgsConstructor
public class TutorMessagingController {

  private final ConversationRepository convRepo;
  private final MessageRepository msgRepo;
  private final UserRepository userRepo;

  @GetMapping("/conversations")
  public List<ConversationSummary> listConversations(@PathVariable Long tutorId) {
    return convRepo.findByTutor_Id(tutorId).stream().map(c -> {
      Message last = msgRepo.findTop1ByConversationIdOrderByCreatedAtDesc(c.getId());
      String otherName = c.getStudent() != null ? c.getStudent().getName() : "";
      long unread;
      if (c.getTutorLastReadAt() == null) {
        unread = msgRepo.countByConversationIdAndSender_IdNot(c.getId(), tutorId);
      } else {
        unread = msgRepo.countByConversationIdAndCreatedAtAfterAndSender_IdNot(c.getId(), c.getTutorLastReadAt(), tutorId);
      }
      return ConversationSummary.builder()
          .conversationId(c.getId())
          .otherUserId(c.getStudent()!=null? c.getStudent().getId(): null)
          .otherName(otherName)
          .lastMessage(last != null ? last.getBody() : null)
          .lastMessageAt(last != null ? last.getCreatedAt() : null)
          .unreadCount((int) unread)
          .build();
    }).sorted((a,b) -> {
      int ua = a.unreadCount == null ? 0 : a.unreadCount;
      int ub = b.unreadCount == null ? 0 : b.unreadCount;
      int cmpUnread = Integer.compare(ub, ua);
      if (cmpUnread != 0) return cmpUnread;
      OffsetDateTime ad = a.lastMessageAt, bd = b.lastMessageAt;
      if (ad == null && bd == null) return 0;
      if (ad == null) return 1;
      if (bd == null) return -1;
      return bd.compareTo(ad);
    }).collect(Collectors.toList());
  }

  @GetMapping("/conversations/{conversationId}/messages")
  public List<MessageDto> getMessages(@PathVariable Long conversationId) {
    return msgRepo.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
        .map(MessageDto::from).toList();
  }

  @PostMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<MessageDto> send(@PathVariable Long tutorId,
                                         @PathVariable Long conversationId,
                                         @RequestBody SendReq req) {
    Conversation conv = convRepo.findById(conversationId).orElseThrow();
    User sender = userRepo.findById(tutorId).orElseThrow();
    Message m = Message.builder().conversation(conv).sender(sender).body(req.body).build();
    m = msgRepo.save(m);
    conv.setTutorLastReadAt(OffsetDateTime.now());
    convRepo.save(conv);
    return ResponseEntity.ok(MessageDto.from(m));
  }

  @PutMapping("/conversations/{conversationId}/read")
  public ResponseEntity<Void> markRead(@PathVariable Long tutorId, @PathVariable Long conversationId){
    Conversation conv = convRepo.findById(conversationId).orElseThrow();
    conv.setTutorLastReadAt(OffsetDateTime.now());
    convRepo.save(conv);
    return ResponseEntity.noContent().build();
  }

  @Builder
  public static class ConversationSummary {
    public Long conversationId;
    public Long otherUserId;
    public String otherName;
    public String lastMessage;
    public OffsetDateTime lastMessageAt;
    public Integer unreadCount;
  }

  @Builder
  public static class MessageDto {
    public Long id;
    public Long conversationId;
    public Long senderUserId;
    public String body;
    public OffsetDateTime createdAt;
    public static MessageDto from(Message m){
      return MessageDto.builder()
          .id(m.getId())
          .conversationId(m.getConversation().getId())
          .senderUserId(m.getSender().getId())
          .body(m.getBody())
          .createdAt(m.getCreatedAt())
          .build();
    }
  }

  public static class SendReq { public String body; }
}
