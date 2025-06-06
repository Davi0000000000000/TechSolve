package br.ifsp.edu.TechSolve.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.ifsp.edu.TechSolve.dto.authentication.UserRegistrationDTO;
import br.ifsp.edu.TechSolve.exception.ErrorResponse;
import br.ifsp.edu.TechSolve.exception.ResourceNotFoundException;
import br.ifsp.edu.TechSolve.model.Role;
import br.ifsp.edu.TechSolve.model.User;
import br.ifsp.edu.TechSolve.model.enumerations.ERole;
import br.ifsp.edu.TechSolve.repository.RoleRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;
import br.ifsp.edu.TechSolve.service.UserService;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, UserRepository userRepository, RoleRepository roleRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationDTO userDto) {
        User user = userService.registerUser(userDto);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/admin/update-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @RequestParam String username,
            @RequestParam ERole newRole
    ) {
        try {
            System.out.println("Iniciando updateUserRole para: " + username + " -> " + newRole);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
            System.out.println("Usuário encontrado: " + user.getUsername());

            Role role = roleRepository.findByRoleName(newRole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + newRole.name()));
            System.out.println("Role encontrado: " + role.getRoleName());

            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);

            return ResponseEntity.ok("User role updated to " + newRole.name());

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Erro interno: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }





}
