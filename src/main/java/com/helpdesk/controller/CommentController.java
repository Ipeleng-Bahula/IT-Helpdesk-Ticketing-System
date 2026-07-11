package com.helpdesk.controller;

import com.helpdesk.model.Comment;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.repository.CommentRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long ticketId,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        ticketService.ensureCanViewTicket(ticketId, userDetails.getUsername());
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return ResponseEntity.ok(commentRepo.findByTicketOrderByCreatedAtAsc(ticket));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable Long ticketId,
                                               @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        ticketService.ensureCanViewTicket(ticketId, userDetails.getUsername());
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User author = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .content(body.get("content"))
                .ticket(ticket)
                .author(author)
                .build();

        return ResponseEntity.ok(commentRepo.save(comment));
    }
}