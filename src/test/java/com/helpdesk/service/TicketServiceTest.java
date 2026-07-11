package com.helpdesk.service;

import com.helpdesk.model.Role;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void ownerCanViewOwnTicket() {
        User owner = user(1L, "owner", Role.USER);
        Ticket ticket = ticket(10L, owner, null);

        when(ticketRepo.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepo.findByUsername("owner")).thenReturn(Optional.of(owner));

        assertDoesNotThrow(() -> ticketService.getTicketForUser(10L, "owner"));
    }

    @Test
    void technicianCanViewAnyTicket() {
        User owner = user(1L, "owner", Role.USER);
        User technician = user(2L, "tech", Role.TECHNICIAN);
        Ticket ticket = ticket(10L, owner, null);

        when(ticketRepo.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepo.findByUsername("tech")).thenReturn(Optional.of(technician));

        assertDoesNotThrow(() -> ticketService.getTicketForUser(10L, "tech"));
    }

    @Test
    void userCannotViewSomeoneElsesTicket() {
        User owner = user(1L, "owner", Role.USER);
        User otherUser = user(2L, "other", Role.USER);
        Ticket ticket = ticket(10L, owner, null);

        when(ticketRepo.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepo.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThrows(SecurityException.class, () -> ticketService.getTicketForUser(10L, "other"));
    }

    @Test
    void ticketCannotBeAssignedToRegularUser() {
        User owner = user(1L, "owner", Role.USER);
        User regularUser = user(2L, "user", Role.USER);
        Ticket ticket = ticket(10L, owner, null);

        when(ticketRepo.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepo.findById(2L)).thenReturn(Optional.of(regularUser));

        assertThrows(RuntimeException.class, () -> ticketService.assignTicket(10L, 2L));
    }

    private User user(Long id, String username, Role role) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@example.com")
                .password("password")
                .role(role)
                .build();
    }

    private Ticket ticket(Long id, User owner, User assignedTo) {
        return Ticket.builder()
                .id(id)
                .title("Laptop issue")
                .description("Laptop will not start")
                .createdBy(owner)
                .assignedTo(assignedTo)
                .build();
    }
}
