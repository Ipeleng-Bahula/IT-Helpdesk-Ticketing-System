package com.helpdesk.service;

import com.helpdesk.dto.TicketRequest;
import com.helpdesk.model.*;
import com.helpdesk.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    public Ticket createTicket(TicketRequest req, String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Ticket ticket = Ticket.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .priority(req.getPriority() != null ? req.getPriority() : Ticket.TicketPriority.MEDIUM)
                .createdBy(user)
                .build();
        return ticketRepo.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepo.findAll();
    }

    public List<Ticket> getMyTickets(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return ticketRepo.findByCreatedBy(user);
    }

    public Ticket getTicketById(Long id) {
        return ticketRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    public Ticket getTicketForUser(Long id, String username) {
        Ticket ticket = getTicketById(id);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (canManageTickets(user) || isOwner(ticket, user) || isAssignedTo(ticket, user)) {
            return ticket;
        }

        throw new SecurityException("You do not have access to this ticket");
    }

    public void ensureCanViewTicket(Long id, String username) {
        getTicketForUser(id, username);
    }

    public Ticket updateStatus(Long id, Ticket.TicketStatus status) {
        Ticket ticket = getTicketById(id);
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepo.save(ticket);
    }

    public Ticket assignTicket(Long ticketId, Long technicianId) {
        Ticket ticket = getTicketById(ticketId);
        User tech = userRepo.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + technicianId));
        if (!canManageTickets(tech)) {
            throw new RuntimeException("Tickets can only be assigned to a technician or admin");
        }
        ticket.setAssignedTo(tech);
        ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepo.save(ticket);
    }

    public void deleteTicket(Long id) {
        if (!ticketRepo.existsById(id)) {
            throw new RuntimeException("Ticket not found with id: " + id);
        }
        ticketRepo.deleteById(id);
    }

    private boolean canManageTickets(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.TECHNICIAN;
    }

    private boolean isOwner(Ticket ticket, User user) {
        return ticket.getCreatedBy() != null && ticket.getCreatedBy().getId().equals(user.getId());
    }

    private boolean isAssignedTo(Ticket ticket, User user) {
        return ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId());
    }
}
