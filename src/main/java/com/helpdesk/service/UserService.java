package com.helpdesk.service;

import com.helpdesk.model.Role;
import com.helpdesk.model.User;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final TicketRepository ticketRepo;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updateRole(Long id, Role role) {
        User user = getUserById(id);
        user.setRole(role);
        return userRepo.save(user);
    }

    public List<User> getTechnicians() {
        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.TECHNICIAN || u.getRole() == Role.ADMIN)
                .toList();
    }

    public void deleteUser(Long id, String currentUsername) {
        User user = getUserById(id);

        if (user.getUsername().equals(currentUsername)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        boolean hasCreatedTickets = !ticketRepo.findByCreatedBy(user).isEmpty();
        boolean hasAssignedTickets = !ticketRepo.findByAssignedTo(user).isEmpty();
        if (hasCreatedTickets || hasAssignedTickets) {
            throw new RuntimeException(
                "Cannot delete this user — they have existing tickets. " +
                "Reassign or resolve their tickets first."
            );
        }

        userRepo.delete(user);
    }
}