package com.helpdesk.dto;
import com.helpdesk.model.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be 200 characters or less")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Ticket.TicketPriority priority;
}
