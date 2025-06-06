package br.ifsp.edu.TechSolve.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ifsp.edu.TechSolve.dto.dashboard.PrioritizedTicketDTO;
import br.ifsp.edu.TechSolve.dto.dashboard.TechnicianSlaStatsDTO;
import br.ifsp.edu.TechSolve.dto.page.PagedResponse;
import br.ifsp.edu.TechSolve.dto.ticket.TicketObservationUpdateDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketRequestDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketResponseDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketStatusUpdateDTO;
import br.ifsp.edu.TechSolve.model.Ticket;
import br.ifsp.edu.TechSolve.model.UserAuthenticated;
import br.ifsp.edu.TechSolve.model.enumerations.ETicketStatus;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import br.ifsp.edu.TechSolve.service.TicketService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
 
    }
    
    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketRequestDTO ticket,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        TicketResponseDTO ticketResponseDTO = ticketService.createTicket(ticket, authenticatedUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketResponseDTO);
    }
    
    @GetMapping
    public ResponseEntity<PagedResponse<TicketResponseDTO>> getAllUserTickets(Pageable pageable,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser ) {
        return ResponseEntity.ok(ticketService.getAllTicketsByUser(pageable, authenticatedUser.getUser()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")   
    public ResponseEntity<PagedResponse<TicketResponseDTO>> getAllTickets(Pageable pageable) {
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        return ResponseEntity.ok(ticketService.getTicketById(id, authenticatedUser.getUser()));
    }
    
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<TicketResponseDTO>> searchByDepartment(@RequestParam String category,
            Pageable pageable,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        return ResponseEntity.ok(ticketService.searchByDepartment(category, pageable, authenticatedUser.getUser()));
    }
    
    @PatchMapping("/{id}/finish")
    public ResponseEntity<TicketResponseDTO> concludeTicket(@PathVariable Long id,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        TicketResponseDTO response = ticketService.concludeTicket(id, authenticatedUser.getUser());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketRequestDTO ticketDto,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        TicketResponseDTO updatedTicket = ticketService.updateTicket(id, ticketDto, authenticatedUser.getUser());
        return ResponseEntity.ok(updatedTicket);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        	ticketService.deleteTicket(id, authenticatedUser.getUser());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<PagedResponse<TicketResponseDTO>> getAvailableTickets(Pageable pageable) {
        return ResponseEntity.ok(ticketService.getAvailableTickets(pageable));
    }
    
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<PagedResponse<TicketResponseDTO>> getAssignedTickets(
            Pageable pageable,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        return ResponseEntity.ok(ticketService.getAssignedTickets(pageable, authenticatedUser.getUser()));
    }
    
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<TicketResponseDTO> assignTicketToTechnician(
            @PathVariable Long id,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        TicketResponseDTO dto = ticketService.assignTicketToTechnician(id, authenticatedUser.getUser());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<TicketResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateDTO dto,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {

        TicketResponseDTO updated = ticketService.updateTicketStatus(id, dto.getStatus(), authenticatedUser.getUser());
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/observations")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<TicketResponseDTO> addObservation(
            @PathVariable Long id,
            @Valid @RequestBody TicketObservationUpdateDTO dto,
            @AuthenticationPrincipal UserAuthenticated authenticatedUser) {

        TicketResponseDTO updated = ticketService.addTechnicalNotes(id, dto.getTechnicalNotes(), authenticatedUser.getUser());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponseDTO>> getTicketsByStatus(
            @RequestParam("value") ETicketStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status, pageable));
    }
    
    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponseDTO>> filterTickets(
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) ETicketStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(
                ticketService.filterTickets(technicianId, priority, status, pageable)
        );
    }

    @GetMapping("/sla-timeexceeded")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponseDTO>> getSlaViolatedTickets(Pageable pageable) {
        return ResponseEntity.ok(ticketService.getSlaViolatedTickets(pageable));
    }

    @GetMapping("/dashboard/sla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TechnicianSlaStatsDTO>> getSlaStatsByTechnician() {
        return ResponseEntity.ok(ticketService.getSlaStatsByTechnician());
    }
    
    @GetMapping("/prioritized")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrioritizedTicketDTO>> getPrioritizedTickets() {
        return ResponseEntity.ok(ticketService.getPrioritizedTickets());
    }






}