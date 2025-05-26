package br.ifsp.edu.TechSolve.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.edu.TechSolve.dto.ticket.TicketObservationUpdateDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketRequestDTO;
import br.ifsp.edu.TechSolve.dto.ticket.TicketStatusUpdateDTO;
import br.ifsp.edu.TechSolve.model.Ticket;
import br.ifsp.edu.TechSolve.model.enumerations.Department;
import br.ifsp.edu.TechSolve.model.enumerations.ETicketStatus;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;
import br.ifsp.edu.TechSolve.repository.TicketRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;
import br.ifsp.edu.TechSolve.util.TestDataHelper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TicketControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataHelper testDataHelper;

    @BeforeEach
    void setUpEnvironment() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        testDataHelper.setupTestUsers();
    }

    @Test
    void shouldCreateTicketForAuthenticatedUser() throws Exception {
        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("My Ticket");
        dto.setPriority(Priority.HIGH);
        dto.setDueDate(LocalDateTime.now().plusDays(1));
        dto.setDepartment(Department.IT);

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("My Ticket"));

        var ticket = ticketRepository.findAll();
        assertFalse(ticket.isEmpty(), "Ticket list should not be empty after creation");
        assertEquals(testDataHelper.getNormalUser().getId(), ticket.get(0).getUser().getId(),
                "Ticket should be assigned to the correct user");
    }

    @Test
    void shouldFailWithPastDueDateWhenCreatingTicket() throws Exception {
        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setTitle("Invalid Ticket");
        dto.setPriority(Priority.MEDIUM);
        dto.setDueDate(LocalDateTime.now().minusDays(1));
        dto.setDepartment(Department.LOGISTICS);

        mockMvc.perform(post("/api/tickets")
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void normalUserShouldGetOwnTicketById() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Normal User's Ticket");
        ticket.setPriority(Priority.LOW);
        ticket.setDueDate(LocalDateTime.now().plusDays(2));
        ticket.setDepartment(Department.OTHER);
        ticket.setUser(testDataHelper.getNormalUser());
        ticket.setCreatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        mockMvc.perform(get("/api/ticket/" + savedTicket.getId())
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Normal User's Ticket"));
    }

    @Test
    void adminUserShouldGetAnyTicketById() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Tickets for Admin to See");
        ticket.setPriority(Priority.LOW);
        ticket.setDueDate(LocalDateTime.now().plusDays(2));
        ticket.setDepartment(Department.OTHER);
        ticket.setUser(testDataHelper.getNormalUser());
        ticket.setCreatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        mockMvc.perform(get("/api/ticket/" + savedTicket.getId())
                .header("Authorization", "Bearer " + testDataHelper.getAdminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Ticket for Admin to See"));
    }

    @Test
    void normalUserShouldNotGetAnothersTicketById() throws Exception {
        Ticket otherUserTicket = new Ticket();
        otherUserTicket.setTitle("Admin's Private Ticket");
        otherUserTicket.setPriority(Priority.HIGH);
        otherUserTicket.setDueDate(LocalDateTime.now().plusDays(1));
        otherUserTicket.setDepartment(Department.LOGISTICS);
        otherUserTicket.setUser(testDataHelper.getAdminUser());
        otherUserTicket.setCreatedAt(LocalDateTime.now());
        Ticket savedOtherUserTicket = ticketRepository.save(otherUserTicket);

        mockMvc.perform(get("/api/tickets/" + savedOtherUserTicket.getId())
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotDeleteCompletedTicketWhileAuthenticated() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Done otherUserTicket");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setDueDate(LocalDateTime.now().plusDays(2));
        ticket.setDepartment(Department.HR);
        ticket.setCompleted(true);
        ticket.setUser(testDataHelper.getNormalUser());
        ticket.setCreatedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        mockMvc.perform(delete("/api/ticket/" + saved.getId())
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void normalUserShouldListOnlyOwnTicketWithPagination() throws Exception {
        for (int i = 0; i < 3; i++) {
            Ticket ticket = new Ticket();
            ticket.setTitle("NormalUser otherUserTicket " + i);
            ticket.setPriority(Priority.MEDIUM);
            ticket.setDueDate(LocalDateTime.now().plusDays(3));
            ticket.setDepartment(Department.OPERATIONS);
            ticket.setUser(testDataHelper.getNormalUser());
            ticket.setCreatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
        for (int i = 0; i < 2; i++) {
            Ticket ticket = new Ticket();
            ticket.setTitle("AdminUser Ticket " + i);
            ticket.setPriority(Priority.MEDIUM);
            ticket.setDueDate(LocalDateTime.now().plusDays(3));
            ticket.setDepartment(Department.LOGISTICS);
            ticket.setUser(testDataHelper.getAdminUser());
            ticket.setCreatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }

        mockMvc.perform(get("/api/tickets?page=0&size=5")
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void adminUserShouldListAllTicketsWithPagination() throws Exception {
        for (int i = 0; i < 3; i++) {
            Ticket ticket = new Ticket();
            ticket.setTitle("NormalUser Ticket " + i);
            ticket.setPriority(Priority.MEDIUM);
            ticket.setDueDate(LocalDateTime.now().plusDays(3));
            ticket.setDepartment(Department.OPERATIONS);
            ticket.setUser(testDataHelper.getNormalUser());
            ticket.setCreatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
        for (int i = 0; i < 2; i++) {
            Ticket ticket = new Ticket();
            ticket.setTitle("AdminUser Ticket " + i);
            ticket.setPriority(Priority.MEDIUM);
            ticket.setDueDate(LocalDateTime.now().plusDays(3));
            ticket.setDepartment(Department.LOGISTICS);
            ticket.setUser(testDataHelper.getAdminUser());
            ticket.setCreatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }

        mockMvc.perform(get("/api/tickets/all?page=0&size=5")
                .header("Authorization", "Bearer " + testDataHelper.getAdminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void normalUserShouldSearchByDepartmentOnlyOwnTickets() throws Exception {
        Ticket ticketUser = new Ticket();
        ticketUser.setTitle("Normal User LOGISTICS Tickets");
        ticketUser.setPriority(Priority.MEDIUM);
        ticketUser.setDueDate(LocalDateTime.now().plusDays(2));
        ticketUser.setDepartment(Department.LOGISTICS);
        ticketUser.setUser(testDataHelper.getNormalUser());
        ticketUser.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(ticketUser);

        Ticket ticketAdmin = new Ticket();
        ticketAdmin.setTitle("Admin User LOGISTICS Tickets");
        ticketAdmin.setPriority(Priority.HIGH);
        ticketAdmin.setDueDate(LocalDateTime.now().plusDays(2));
        ticketAdmin.setDepartment(Department.LOGISTICS);
        ticketAdmin.setUser(testDataHelper.getAdminUser());
        ticketAdmin.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(ticketAdmin);

        Ticket ticketUserOPERATIONS = new Ticket();
        ticketUserOPERATIONS.setTitle("Normal User OPERATIONS Tickets");
        ticketUserOPERATIONS.setPriority(Priority.LOW);
        ticketUserOPERATIONS.setDueDate(LocalDateTime.now().plusDays(2));
        ticketUserOPERATIONS.setDepartment(Department.OPERATIONS);
        ticketUserOPERATIONS.setUser(testDataHelper.getNormalUser());
        ticketUserOPERATIONS.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(ticketUserOPERATIONS);

        mockMvc.perform(get("/api/tickets/search")
                .param("department", "LOGISTICS")
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Normal User LOGISTICS Tickets"));
    }

    @Test
    void adminUserShouldSearchByDepartmentAllTickets() throws Exception {
        Ticket ticketUser = new Ticket();
        ticketUser.setTitle("Normal User LOGISTICS Ticket");
        ticketUser.setPriority(Priority.MEDIUM);
        ticketUser.setDueDate(LocalDateTime.now().plusDays(2));
        ticketUser.setDepartment(Department.LOGISTICS);
        ticketUser.setUser(testDataHelper.getNormalUser());
        ticketUser.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(ticketUser);

        Ticket ticketAdmin = new Ticket();
        ticketAdmin.setTitle("Admin User LOGISTICS Ticket");
        ticketAdmin.setPriority(Priority.HIGH);
        ticketAdmin.setDueDate(LocalDateTime.now().plusDays(2));
        ticketAdmin.setDepartment(Department.LOGISTICS);
        ticketAdmin.setUser(testDataHelper.getAdminUser());
        ticketAdmin.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(ticketAdmin);

        Ticket ticketUserOPERATIONS = new Ticket();
        ticketUserOPERATIONS.setTitle("Normal User OPERATIONS Ticket");
        ticketUserOPERATIONS.setPriority(Priority.LOW);
        ticketUserOPERATIONS.setDueDate(LocalDateTime.now().plusDays(2));
        ticketUserOPERATIONS.setDepartment(Department.OPERATIONS);
        ticketUserOPERATIONS.setUser(testDataHelper.getNormalUser());
        ticketUserOPERATIONS.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(ticketUserOPERATIONS);

        mockMvc.perform(get("/api/tickets/search")
                .param("department", "LOGISTICS")
                .header("Authorization", "Bearer " + testDataHelper.getAdminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }
    
    @Test
    @WithMockUser(username = "tecnico", roles = "TECHNICIAN")
    void technicianShouldAssignAvailableTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado disponível");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.IT);
        ticket.setStatus(ETicketStatus.ABERTO);
        ticket.setCompleted(false);
        Ticket saved = ticketRepository.save(ticket);

        mockMvc.perform(put("/api/tickets/" + saved.getId() + "/assign")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
    }

    @Test
    @WithMockUser(username = "tecnico", roles = "TECHNICIAN")
    void technicianShouldUpdateStatusToResolved() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado em andamento");
        ticket.setPriority(Priority.HIGH);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.IT);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setCompleted(false);
        ticket.setAssignedTechnician(testDataHelper.getTechnicianUser());
        Ticket saved = ticketRepository.save(ticket);

        TicketStatusUpdateDTO dto = new TicketStatusUpdateDTO();
        dto.setStatus(ETicketStatus.RESOLVIDO);

        mockMvc.perform(patch("/api/tickets/" + saved.getId() + "/status")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"));
    }
    
    @Test
    @WithMockUser(username = "tecnico", roles = "TECHNICIAN")
    void technicianShouldAddObservation() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado com anotação");
        ticket.setPriority(Priority.LOW);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.IT);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setAssignedTechnician(testDataHelper.getTechnicianUser());
        ticket.setCompleted(false);
        Ticket saved = ticketRepository.save(ticket);

        TicketObservationUpdateDTO dto = new TicketObservationUpdateDTO();
        dto.setTechnicalNotes("Verificação inicial concluída");

        mockMvc.perform(patch("/api/tickets/" + saved.getId() + "/observations")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicalNotes").value("Verificação inicial concluída"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    void userShouldConcludeOwnTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado encerrável");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.LOGISTICS);
        ticket.setUser(testDataHelper.getNormalUser());
        ticket.setCompleted(false);
        Ticket saved = ticketRepository.save(ticket);

        mockMvc.perform(patch("/api/tickets/" + saved.getId() + "/finish")
                .header("Authorization", "Bearer " + testDataHelper.getNormalUserToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }
    
    @Test
    @WithMockUser(username = "tecnico", roles = "TECHNICIAN")
    void technicianShouldAssignAndResolveTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado para atendimento");
        ticket.setPriority(Priority.HIGH);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.IT);
        ticket.setStatus(ETicketStatus.ABERTO);
        ticket.setCompleted(false);
        Ticket saved = ticketRepository.save(ticket);

        // Assumir chamado
        mockMvc.perform(put("/api/tickets/" + saved.getId() + "/assign")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));

        // Resolver chamado
        TicketStatusUpdateDTO dto = new TicketStatusUpdateDTO();
        dto.setStatus(ETicketStatus.RESOLVIDO);

        mockMvc.perform(patch("/api/tickets/" + saved.getId() + "/status")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"));
    }

    @Test
    @WithMockUser(username = "tecnico", roles = "TECHNICIAN")
    void shouldFailWhenAssigningAlreadyAssignedTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado já atribuído");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.IT);
        ticket.setStatus(ETicketStatus.EM_ATENDIMENTO);
        ticket.setCompleted(false);
        ticket.setAssignedTechnician(testDataHelper.getTechnicianUser());
        Ticket saved = ticketRepository.save(ticket);

        mockMvc.perform(put("/api/tickets/" + saved.getId() + "/assign")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "tecnico", roles = "TECHNICIAN")
    void shouldFailWhenUpdatingStatusOfCompletedTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Chamado já finalizado");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setDueDate(LocalDateTime.now().plusDays(1));
        ticket.setDepartment(Department.IT);
        ticket.setStatus(ETicketStatus.RESOLVIDO);
        ticket.setCompleted(true);
        ticket.setAssignedTechnician(testDataHelper.getTechnicianUser());
        Ticket saved = ticketRepository.save(ticket);

        TicketStatusUpdateDTO dto = new TicketStatusUpdateDTO();
        dto.setStatus(ETicketStatus.EM_ATENDIMENTO);

        mockMvc.perform(patch("/api/tickets/" + saved.getId() + "/status")
                .header("Authorization", "Bearer " + testDataHelper.getTechnicianToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    
    
    
}