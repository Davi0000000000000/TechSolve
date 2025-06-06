package br.ifsp.edu.TechSolve.dto.ticket;

import java.time.LocalDateTime;

import br.ifsp.edu.TechSolve.model.enumerations.Department;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequestDTO {
    @NotBlank
    @Size(min = 10, max = 100)
    private String title;

    @Size(max = 255)
    private String description;

    @NotNull
    private Priority priority;

    @NotNull
    @FutureOrPresent
    private LocalDateTime dueDate;

    private boolean completed;

    @NotNull
    private Department department;
}