package br.ifsp.edu.TechSolve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ifsp.edu.TechSolve.model.Role;
import br.ifsp.edu.TechSolve.model.enumerations.ERole;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(ERole roleName);
}
