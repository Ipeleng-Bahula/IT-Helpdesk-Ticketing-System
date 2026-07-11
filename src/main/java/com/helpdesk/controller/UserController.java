package com.helpdesk.controller;

import com.helpdesk.model.Role;
import com.helpdesk.model.User;
import com.helpdesk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<List<User>> getTechnicians() {
        return ResponseEntity.ok(userService.getTechnicians());
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateRole(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        Role role = Role.valueOf(body.get("role"));
        return ResponseEntity.ok(userService.updateRole(id, role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails currentUser) {
        userService.deleteUser(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}