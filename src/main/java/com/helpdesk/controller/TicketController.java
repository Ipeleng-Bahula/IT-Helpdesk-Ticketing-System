package com.helpdesk.controller;

import com.helpdesk.dto.TicketRequest;
import com.helpdesk.model.Ticket;
import com.helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> create(@Valid @RequestBody TicketRequest req,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ticketService.createTicket(req, user.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<List<Ticket>> getAll() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> getMy(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ticketService.getMyTickets(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getOne(@PathVariable Long id,
                                         @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ticketService.getTicketForUser(id, user.getUsername()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Ticket> updateStatus(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        Ticket.TicketStatus status = Ticket.TicketStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ticketService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/assign/{techId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<Ticket> assign(@PathVariable Long id, @PathVariable Long techId) {
        return ResponseEntity.ok(ticketService.assignTicket(id, techId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}