package br.ifsp.edu.TechSolve.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.ifsp.edu.TechSolve.model.Role;
import br.ifsp.edu.TechSolve.model.User;
import br.ifsp.edu.TechSolve.model.enumerations.ERole;
import br.ifsp.edu.TechSolve.repository.RoleRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;
import br.ifsp.edu.TechSolve.service.JwtService;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TestDataHelper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private User normalUserInstance;
    private String normalUserTokenInstance;
    private User adminUserInstance;
    private String adminTokenInstance;
    private User technicianUserInstance;
    private String technicianTokenInstance;


    private static final AtomicInteger userCounter = new AtomicInteger(0);

    public void setupTestUsers() {

        Role userRoleEntity = roleRepository.findByRoleName(ERole.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(ERole.USER);
                    return roleRepository.save(newRole);
                });
        Role adminRoleEntity = roleRepository.findByRoleName(ERole.ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(ERole.ADMIN);
                    return roleRepository.save(newRole);
                });
        Role technicianRoleEntity = roleRepository.findByRoleName(ERole.TECHNICIAN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(ERole.TECHNICIAN);
                    return roleRepository.save(newRole);
                });


        int count = userCounter.getAndIncrement();
        String normalUsername = "testuser" + count;
        // Garantir que não exceda o limite de 20 caracteres
        if (normalUsername.length() > 20) {
            normalUsername = normalUsername.substring(0, 20);
        }
        
        count = userCounter.getAndIncrement();
        String technicianUsername = "techusr" + count;
        if (technicianUsername.length() > 20) {
            technicianUsername = technicianUsername.substring(0, 20);
        }

        technicianUserInstance = new User();
        technicianUserInstance.setUsername(technicianUsername);
        technicianUserInstance.setPassword(passwordEncoder.encode("password"));
        technicianUserInstance.setEmail(technicianUsername + "@example.com");
        technicianUserInstance.setRoles(Set.of(technicianRoleEntity, userRoleEntity));
        userRepository.save(technicianUserInstance);
        technicianTokenInstance = jwtService.generateToken(technicianUserInstance);


        normalUserInstance = new User();
        normalUserInstance.setUsername(normalUsername);
        normalUserInstance.setPassword(passwordEncoder.encode("password"));
        normalUserInstance.setEmail(normalUsername + "@example.com");
        normalUserInstance.setRoles(Set.of(userRoleEntity));
        userRepository.save(normalUserInstance);
        normalUserTokenInstance = jwtService.generateToken(normalUserInstance);

        count = userCounter.getAndIncrement();
        String adminUsername = "adminusr" + count;
        if (adminUsername.length() > 20) {
            adminUsername = adminUsername.substring(0, 20);
        }
        adminUserInstance = new User();
        adminUserInstance.setUsername(adminUsername);
        adminUserInstance.setPassword(passwordEncoder.encode("password"));
        adminUserInstance.setEmail(adminUsername + "@example.com");
        adminUserInstance.setRoles(Set.of(adminRoleEntity, userRoleEntity));
        userRepository.save(adminUserInstance);
        adminTokenInstance = jwtService.generateToken(adminUserInstance);
    }

    public User getNormalUser() {
        if (normalUserInstance == null)
            throw new IllegalStateException("Test users not set up. Call setupTestUsers() first.");
        return normalUserInstance;
    }

    public String getNormalUserToken() {
        if (normalUserTokenInstance == null)
            throw new IllegalStateException("Test users not set up. Call setupTestUsers() first.");
        return normalUserTokenInstance;
    }

    public User getAdminUser() {
        if (adminUserInstance == null)
            throw new IllegalStateException("Test users not set up. Call setupTestUsers() first.");
        return adminUserInstance;
    }

    public String getAdminToken() {
        if (adminTokenInstance == null)
            throw new IllegalStateException("Test users not set up. Call setupTestUsers() first.");
        return adminTokenInstance;
    }
    public User getTechnicianUser() {
        if (technicianUserInstance == null)
            throw new IllegalStateException("Technician not set up. Call setupTestUsers() first.");
        return technicianUserInstance;
    }

    public String getTechnicianToken() {
        if (technicianTokenInstance == null)
            throw new IllegalStateException("Technician not set up. Call setupTestUsers() first.");
        return technicianTokenInstance;
    }

}