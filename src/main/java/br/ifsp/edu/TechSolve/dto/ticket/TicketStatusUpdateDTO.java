package br.ifsp.edu.TechSolve.dto.ticket;

import br.ifsp.edu.TechSolve.model.enumerations.ETicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketStatusUpdateDTO {
    @NotNull
    private ETicketStatus status;
}
