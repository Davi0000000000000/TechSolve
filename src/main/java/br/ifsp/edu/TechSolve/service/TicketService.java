package br.ifsp.edu.TechSolve.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.ifsp.edu.TechSolve.dto.dashboard.PrioritizedTicketDTO;
import br.ifsp.edu.TechSolve.dto.dashboard.TechnicianSlaStatsDTO;
import br.ifsp.edu.TechSolve.dto.page.PagedResponse;
import br.ifsp.edu.TechSolve.dto.ticket.TicketRequestDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketResponseDTO;
import br.ifsp.edu.TechSolve.exception.InvalidTicketStateException;
import br.ifsp.edu.TechSolve.exception.ResourceNotFoundException;
import br.ifsp.edu.TechSolve.mapper.PagedResponseMapper;
import br.ifsp.edu.TechSolve.model.Ticket;
import br.ifsp.edu.TechSolve.model.User;
import br.ifsp.edu.TechSolve.model.enumerations.Department;
import br.ifsp.edu.TechSolve.model.enumerations.ERole;
import br.ifsp.edu.TechSolve.model.enumerations.ETicketStatus;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import br.ifsp.edu.TechSolve.repository.TicketRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;
import jakarta.validation.ValidationException;
import java.time.Duration;


@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final ModelMapper modelMapper;
    private final PagedResponseMapper pagedResponseMapper;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, ModelMapper modelMapper,
                       PagedResponseMapper pagedResponseMapper, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.modelMapper = modelMapper;
        this.pagedResponseMapper = pagedResponseMapper;
        this.userRepository = userRepository;
    }

    public TicketResponseDTO createTicket(TicketRequestDTO ticketDto, User userFromToken) {
        if (ticketDto.getDueDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Due date cannot be in the past.");
        }

        User user = userRepository.findById(userFromToken.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userFromToken.getId()));

        Ticket ticket = modelMapper.map(ticketDto, Ticket.class);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setCompleted(false);
        ticket.setUser(user);
        Ticket createdTicket = ticketRepository.save(ticket);
        return modelMapper.map(createdTicket, TicketResponseDTO.class);
    }

    public PagedResponse<TicketResponseDTO> getAllTicketsByUser(Pageable pageable, User userFromToken) {
        Page<Ticket> ticketsPage;
        ticketsPage = ticketRepository.findByUser(userFromToken, pageable);
        return pagedResponseMapper.toPagedResponse(ticketsPage, TicketResponseDTO.class);
    }

    public PagedResponse<TicketResponseDTO> getAllTickets(Pageable pageable) {
        Page<Ticket> ticketsPage;
        ticketsPage = ticketRepository.findAll(pageable);
        return pagedResponseMapper.toPagedResponse(ticketsPage, TicketResponseDTO.class);
    }

    public TicketResponseDTO getTicketById(Long id, User userFromToken) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!isAdmin(userFromToken) && !ticket.getUser().getId().equals(userFromToken.getId())) {
            throw new AccessDeniedException("You cannot access another user's ticket.");
        }

        return modelMapper.map(ticket, TicketResponseDTO.class);
    }

    public PagedResponse<TicketResponseDTO> searchByDepartment(String department, Pageable pageable, User userFromToken) {
        Department departmentEnum = Department.fromString(department);
        Page<Ticket> ticketsPage;

        if (isAdmin(userFromToken)) {
            ticketsPage = ticketRepository.findByDepartment(departmentEnum, pageable);
        } else {
            ticketsPage = ticketRepository.findByDepartmentAndUser(departmentEnum, userFromToken, pageable);
        }

        return pagedResponseMapper.toPagedResponse(ticketsPage, TicketResponseDTO.class);
    }

    public TicketResponseDTO concludeTicket(Long id, User userFromToken) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));

        validateAccess(ticket, userFromToken);

        if (ticket.isCompleted()) {
            throw new InvalidTicketStateException("Ticket is already completed.");
        }

        ticket.setCompleted(true);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return modelMapper.map(updatedTicket, TicketResponseDTO.class);
    }

    public TicketResponseDTO updateTicket(Long id, TicketRequestDTO ticketDto, User userFromToken) {
        if (ticketDto.getDueDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Due date cannot be in the past");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));

        validateAccess(ticket, userFromToken);

        if (ticket.isCompleted()) {
            throw new InvalidTicketStateException("Completed ticket cannot be updated");
        }

        modelMapper.map(ticketDto, ticket);
        Ticket updatedTicket = ticketRepository.save(ticket);
        return modelMapper.map(updatedTicket, TicketResponseDTO.class);
    }

    public void deleteTicket(Long id, User userFromToken) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));

        validateAccess(ticket, userFromToken);

        if (ticket.isCompleted()) {
            throw new InvalidTicketStateException("Cannot delete a completed ticket");
        }

        ticketRepository.delete(ticket);
    }
    
    public PagedResponse<TicketResponseDTO> getAvailableTickets(Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findByStatus(ETicketStatus.ABERTO, pageable);
        return pagedResponseMapper.toPagedResponse(tickets, TicketResponseDTO.class);
    }


    public PagedResponse<TicketResponseDTO> getAssignedTickets(Pageable pageable, User technician) {
        Page<Ticket> tickets = ticketRepository.findByAssignedTechnician(technician, pageable);
        return pagedResponseMapper.toPagedResponse(tickets, TicketResponseDTO.class);
    }
    
    public TicketResponseDTO assignTicketToTechnician(Long id, User technician) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));

        if (ticket.getAssignedTechnician() != null) {
            throw new InvalidTicketStateException("Ticket is already assigned to another technician.");
            
        }
        
        		

        if (ticket.isCompleted() || !ticket.getStatus().equals(ETicketStatus.ABERTO)) {
            throw new InvalidTicketStateException("Ticket is not available for assignment.");
        }

        ticket.setAssignedTechnician(technician);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        
        System.out.println("[NOTIFICAÇÃO] Chamado ID " + ticket.getId() +
                " atribuído ao técnico " + technician.getUsername());

        TicketResponseDTO dto = modelMapper.map(updatedTicket, TicketResponseDTO.class);
        if (updatedTicket.getAssignedTechnician() != null) {
        	dto.setAssignedTechnicianName(updatedTicket.getAssignedTechnician().getUsername());
        }

        return dto;
    }
    
    public TicketResponseDTO updateTicketStatus(Long ticketId, ETicketStatus newStatus, User technician) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (!technician.equals(ticket.getAssignedTechnician())) {
            throw new AccessDeniedException("You can only update tickets assigned to you.");
        }

        if (ticket.isCompleted()) {
            throw new InvalidTicketStateException("Cannot update a completed ticket.");
        }

        ticket.setStatus(newStatus);

        if (newStatus == ETicketStatus.RESOLVIDO) {
            ticket.setCompleted(true);
            ticket.setResolvedAt(LocalDateTime.now());
        }


        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);

        TicketResponseDTO dto = modelMapper.map(updatedTicket, TicketResponseDTO.class);
        dto.setAssignedTechnicianName(technician.getUsername());
        return dto;
    }
    
    public TicketResponseDTO addTechnicalNotes(Long id, String notes, User technician) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));

        if (!technician.equals(ticket.getAssignedTechnician())) {
            throw new AccessDeniedException("You can only update tickets assigned to you.");
        }	

        ticket.setTechnicalNotes(notes);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        TicketResponseDTO dto = modelMapper.map(saved, TicketResponseDTO.class);
        dto.setAssignedTechnicianName(technician.getUsername());
        return dto;
    }
    
    public PagedResponse<TicketResponseDTO> getTicketsByStatus(ETicketStatus status, Pageable pageable) {
        Page<Ticket> tickets = ticketRepository.findByStatus(status, pageable);
        return pagedResponseMapper.toPagedResponse(tickets, TicketResponseDTO.class);
    }
    
    public PagedResponse<TicketResponseDTO> filterTickets(
            Long technicianId, Priority priority, ETicketStatus status, Pageable pageable) {

        Page<Ticket> tickets = ticketRepository.findByFilters(technicianId, priority, status, pageable);
        return pagedResponseMapper.toPagedResponse(tickets, TicketResponseDTO.class);
    }


    public PagedResponse<TicketResponseDTO> getSlaViolatedTickets(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Ticket> tickets = ticketRepository.findByCompletedFalseAndDueDateBefore(now, pageable);
        return pagedResponseMapper.toPagedResponse(tickets, TicketResponseDTO.class);
    }
    
    public List<TechnicianSlaStatsDTO> getSlaStatsByTechnician() {
        List<Ticket> resolvedTickets = ticketRepository.findByCompletedTrueAndAssignedTechnicianIsNotNull();

        // Agrupar por técnico
        Map<User, List<Ticket>> grouped = resolvedTickets.stream()
            .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
            .collect(Collectors.groupingBy(Ticket::getAssignedTechnician));

        // Calcular média por técnico
        List<TechnicianSlaStatsDTO> stats = new ArrayList<>();

        for (Map.Entry<User, List<Ticket>> entry : grouped.entrySet()) {
            User technician = entry.getKey();
            List<Ticket> tickets = entry.getValue();

            long totalMinutes = tickets.stream()
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes())
                .sum();

            long averageMinutes = tickets.isEmpty() ? 0 : totalMinutes / tickets.size();

            stats.add(new TechnicianSlaStatsDTO(
                technician.getId(),
                technician.getUsername(),
                averageMinutes
            ));
        }

        return stats;
    }
    
    public List<PrioritizedTicketDTO> getPrioritizedTickets() {
        List<Ticket> openTickets = ticketRepository.findByCompletedFalseAndAssignedTechnicianIsNull();

        LocalDateTime now = LocalDateTime.now();

        List<PrioritizedTicketDTO> result = openTickets.stream()
            .map(ticket -> {
                long minutesWaiting = java.time.Duration.between(ticket.getCreatedAt(), now).toMinutes();
                int priorityWeight = switch (ticket.getPriority()) {
                    case LOW -> 1;
                    case MEDIUM -> 2;
                    case HIGH -> 3;
                };
                double score = (minutesWaiting + 1) * priorityWeight;

                return new PrioritizedTicketDTO(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getPriority(),
                    minutesWaiting,
                    score
                );
            })
            .sorted(Comparator.comparingDouble(PrioritizedTicketDTO::getScore).reversed())
            .toList();

        return result;
    }
    
    
    











    // ------------------------------------------------------------------------
    // Métodos auxiliares
    // ------------------------------------------------------------------------

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(ERole.ADMIN));
    }

    private void validateAccess(Ticket ticket, User user) {
        if (!isAdmin(user) && !ticket.getUser().getId().equals(user.getId())) {
            throw new InvalidTicketStateException("You cannot modify another user's ticket.");
        }
    }
}