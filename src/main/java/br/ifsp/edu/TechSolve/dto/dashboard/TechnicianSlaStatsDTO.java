package br.ifsp.edu.TechSolve.dto.dashboard;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TechnicianSlaStatsDTO {
    private Long technicianId;
    private String technicianUsername;
    private long averageResponseTimeMinutes;
}
