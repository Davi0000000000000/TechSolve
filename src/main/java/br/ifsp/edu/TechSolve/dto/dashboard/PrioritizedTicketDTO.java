package br.ifsp.edu.TechSolve.dto.dashboard;

import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrioritizedTicketDTO {
    private Long id;
    private String title;
    private Priority priority;
    private long minutesWaiting;
    private double score;
}
