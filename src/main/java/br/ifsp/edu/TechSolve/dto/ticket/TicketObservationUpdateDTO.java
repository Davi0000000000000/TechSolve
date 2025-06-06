package br.ifsp.edu.TechSolve.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketObservationUpdateDTO {
    @NotBlank
    private String technicalNotes;
}
