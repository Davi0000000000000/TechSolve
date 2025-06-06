package br.ifsp.edu.TechSolve.dto.ticket;

import java.time.LocalDateTime;

import br.ifsp.edu.TechSolve.model.enumerations.Department;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import lombok.Data;

@Data
public class TicketResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime dueDate;
    private boolean completed;
    private Department department;
    private LocalDateTime createdAt;
    private String assignedTechnicianName;
    private String technicalNotes;
    private LocalDateTime resolvedAt;

}
