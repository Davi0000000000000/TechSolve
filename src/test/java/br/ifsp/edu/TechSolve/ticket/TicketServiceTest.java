package br.ifsp.edu.TechSolve.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import br.ifsp.edu.TechSolve.dto.dashboard.TechnicianSlaStatsDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketRequestDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketResponseDTO;
import br.ifsp.edu.TechSolve.exception.InvalidTicketStateException;
import br.ifsp.edu.TechSolve.exception.ResourceNotFoundException;
import br.ifsp.edu.TechSolve.mapper.PagedResponseMapper;
import br.ifsp.edu.TechSolve.model.Ticket;
import br.ifsp.edu.TechSolve.model.User;
import br.ifsp.edu.TechSolve.model.enumerations.Department;
import br.ifsp.edu.TechSolve.model.enumerations.ETicketStatus;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import br.ifsp.edu.TechSolve.repository.TicketRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;
import br.ifsp.edu.TechSolve.service.TicketService;
import jakarta.validation.ValidationException;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PagedResponseMapper pagedResponseMapper;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void shouldCreateTicketkWithValidData() {
        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Valid Ticket");
        dto.setPriority(Priority.MEDIUM);
        dto.setDueDate(LocalDateTime.now().plusDays(2));
        dto.setDepartment(Department.LOGISTICS);

        User mockUser = new User();
        mockUser.setId(1L);

        Ticket ticketEntity = new Ticket();
        Ticket savedTicket = new Ticket();
        savedTicket.setId(1L);
        savedTicket.setTitle("Valid Ticket");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(modelMapper.map(dto, Ticket.class)).thenReturn(ticketEntity);
        when(ticketRepository.save(any())).thenReturn(savedTicket);
        when(modelMapper.map(savedTicket, TicketResponseDTO.class)).thenReturn(new TicketResponseDTO());

        TicketResponseDTO response = ticketService.createTicket(dto, mockUser);
        assertNotNull(response);
    }

    @Test
    void shouldThrowValidationExceptionWhenDueDateIsPast() {
        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Invalid Ticket");
        dto.setDueDate(LocalDateTime.now().minusDays(1));

        User mockUser = new User();
        mockUser.setId(1L);

        assertThrows(ValidationException.class, () -> ticketService.createTicket(dto, mockUser));
    }

    @Test
    void shouldFetchTicketById() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        User mockUser = new User();
        mockUser.setId(1L);

        ticket.setUser(mockUser);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(modelMapper.map(any(), eq(TicketResponseDTO.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO response = ticketService.getTicketById(1L, mockUser);
        assertNotNull(response);
    }

    @Test
    void shouldThrowErrorWhenDeletingCompletedTicket() {
        Ticket completedTicket = new Ticket();
        completedTicket.setId(1L);
        completedTicket.setCompleted(true);

        User mockUser = new User();
        mockUser.setId(1L);

        completedTicket.setUser(mockUser);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(completedTicket));

        assertThrows(InvalidTicketStateException.class, () -> ticketService.deleteTicket(1L, mockUser));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTicketDoesNotExist() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicketById(1L, mockUser));
    }
    
    @Test
    void shouldSetResolvedAtAndCompletedWhenStatusIsResolvido() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setAssignedTechnician(new User());
        ticket.setCompleted(false);

        User technician = ticket.getAssignedTechnician();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(), eq(TicketResponseDTO.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO response = ticketService.updateTicketStatus(1L, ETicketStatus.RESOLVIDO, technician);

        assertNotNull(ticket.getResolvedAt());
        assertNotNull(response);
        assert(ticket.isCompleted());
    }
    
    @Test
    void shouldThrowAccessDeniedWhenTechnicianIsNotAssigned() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        User assigned = new User();
        assigned.setId(1L);
        ticket.setAssignedTechnician(assigned);

        User outsider = new User();
        outsider.setId(2L); 

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class, () ->
            ticketService.updateTicketStatus(1L, ETicketStatus.RESOLVIDO, outsider));
    }
    
    @Test
    void shouldCalculateAverageSlaForTechnician() {
        User technician = new User();
        technician.setId(1L);
        technician.setUsername("tecnico");

        Ticket t1 = new Ticket();
        t1.setCreatedAt(LocalDateTime.now().minusHours(2));
        t1.setResolvedAt(LocalDateTime.now());
        t1.setAssignedTechnician(technician);
        t1.setCompleted(true);

        Ticket t2 = new Ticket();
        t2.setCreatedAt(LocalDateTime.now().minusHours(4));
        t2.setResolvedAt(LocalDateTime.now());
        t2.setAssignedTechnician(technician);
        t2.setCompleted(true);

        when(ticketRepository.findByCompletedTrueAndAssignedTechnicianIsNotNull())
            .thenReturn(List.of(t1, t2));

        List<TechnicianSlaStatsDTO> stats = ticketService.getSlaStatsByTechnician();

        assertNotNull(stats);
        assert(!stats.isEmpty());
        assert(stats.get(0).getTechnicianUsername().equals("tecnico"));
    }
    
    @Test
    void shouldAssignTicketToTechnicianIfAvailable() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(ETicketStatus.ABERTO);
        ticket.setCompleted(false);

        User technician = new User();
        technician.setId(1L);
        technician.setUsername("tech");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(), eq(TicketResponseDTO.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO response = ticketService.assignTicketToTechnician(1L, technician);

        assertNotNull(response);
        assertEquals(ETicketStatus.EM_ATENDIMENTO, ticket.getStatus());
        assertEquals(technician, ticket.getAssignedTechnician());
    }

    @Test
    void shouldThrowWhenTicketAlreadyAssigned() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(ETicketStatus.ABERTO);
        ticket.setCompleted(false);
        ticket.setAssignedTechnician(new User());

        User technician = new User();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class, () ->
            ticketService.assignTicketToTechnician(1L, technician));
    }

    @Test
    void shouldThrowWhenTicketNotOpen() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(ETicketStatus.RESOLVIDO);
        ticket.setCompleted(false);

        User technician = new User();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class, () ->
            ticketService.assignTicketToTechnician(1L, technician));
    }

    @Test
    void shouldUpdateTicketSuccessfully() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCompleted(false);
        ticket.setUser(new User());
        ticket.getUser().setId(1L);

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setDueDate(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);
        when(modelMapper.map(any(), eq(TicketResponseDTO.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO result = ticketService.updateTicket(1L, dto, user);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenUpdatingCompletedTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCompleted(true);
        ticket.setUser(new User());
        ticket.getUser().setId(1L);

        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setDueDate(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class, () -> {
            ticketService.updateTicket(1L, dto, user);
        });
    }

    @Test
    void shouldThrowWhenUpdatingWithPastDueDate() {
        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setDueDate(LocalDateTime.now().minusDays(1));

        User user = new User();
        user.setId(1L);

        assertThrows(ValidationException.class, () -> {
            ticketService.updateTicket(1L, dto, user);
        });
    }

    @Test
    void shouldConcludeTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCompleted(false);
        ticket.setUser(new User());
        ticket.getUser().setId(1L);

        User user = new User();
        user.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);
        when(modelMapper.map(any(), eq(TicketResponseDTO.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO result = ticketService.concludeTicket(1L, user);

        assertNotNull(result);
        assert(ticket.isCompleted());
    }

    @Test
    void shouldThrowWhenConcludingAlreadyCompletedTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCompleted(true);
        ticket.setUser(new User());
        ticket.getUser().setId(1L);

        User user = new User();
        user.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class, () -> {
            ticketService.concludeTicket(1L, user);
        });
    }

    @Test
    void shouldAddTechnicalNotes() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        User tech = new User();
        tech.setId(1L);
        ticket.setAssignedTechnician(tech);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);
        when(modelMapper.map(any(), eq(TicketResponseDTO.class))).thenReturn(new TicketResponseDTO());

        TicketResponseDTO result = ticketService.addTechnicalNotes(1L, "observações técnicas", tech);

        assertNotNull(result);
        assertEquals("observações técnicas", ticket.getTechnicalNotes());
    }

    @Test
    void shouldThrowWhenAddingNotesByUnassignedTechnician() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setAssignedTechnician(new User());
        ticket.getAssignedTechnician().setId(1L);

        User outsider = new User();
        outsider.setId(2L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class, () ->
            ticketService.addTechnicalNotes(1L, "tentativa inválida", outsider));
    }
    
    @Test
    void shouldThrowWhenUpdatingStatusOnCompletedTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCompleted(true);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setAssignedTechnician(new User());
        ticket.getAssignedTechnician().setId(1L);

        User technician = new User();
        technician.setId(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class, () ->
            ticketService.updateTicketStatus(1L, ETicketStatus.RESOLVIDO, technician));
    }

    @Test
    void shouldThrowWhenTechnicianNotAssignedTriesToUpdateStatus() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCompleted(false);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setAssignedTechnician(new User());
        ticket.getAssignedTechnician().setId(1L);

        User intruder = new User();
        intruder.setId(999L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(AccessDeniedException.class, () ->
            ticketService.updateTicketStatus(1L, ETicketStatus.RESOLVIDO, intruder));
    }

    



}