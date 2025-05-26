package br.ifsp.edu.TechSolve.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ifsp.edu.TechSolve.model.Ticket;
import br.ifsp.edu.TechSolve.model.User;
import br.ifsp.edu.TechSolve.model.enumerations.Department;
import br.ifsp.edu.TechSolve.model.enumerations.ETicketStatus;
import br.ifsp.edu.TechSolve.model.enumerations.Priority;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Page<Ticket> findByUser(User user, Pageable pageable);
    Page<Ticket> findByDepartment(Department department, Pageable pageable);
    Page<Ticket> findByDepartmentAndUser(Department department, User user, Pageable pageable);
    Page<Ticket> findByUserId(Long userId, Pageable pageable);
    Page<Ticket> findByStatus(ETicketStatus status, Pageable pageable);
    Page<Ticket> findByAssignedTechnician(User technician, Pageable pageable);
    Page<Ticket> findByCompletedFalseAndDueDateBefore(LocalDateTime now, Pageable pageable);
    List<Ticket> findByCompletedTrueAndAssignedTechnicianIsNotNull();
    List<Ticket> findByCompletedFalseAndAssignedTechnicianIsNull();

    @Query("""
    	    SELECT t FROM Ticket t
    	    WHERE (:technicianId IS NULL OR t.assignedTechnician.id = :technicianId)
    	      AND (:priority IS NULL OR t.priority = :priority)
    	      AND (:status IS NULL OR t.status = :status)
    	""")
    	Page<Ticket> findByFilters(
    	        @Param("technicianId") Long technicianId,
    	        @Param("priority") Priority priority,
    	        @Param("status") ETicketStatus status,
    	        Pageable pageable);


}
