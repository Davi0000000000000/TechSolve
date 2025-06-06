package br.ifsp.edu.TechSolve.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.ifsp.edu.TechSolve.dto.authentication.UserRegistrationDTO;
import br.ifsp.edu.TechSolve.exception.UserAlreadyExistsException;
import br.ifsp.edu.TechSolve.model.Role;
import br.ifsp.edu.TechSolve.model.User;
import br.ifsp.edu.TechSolve.model.enumerations.ERole;
import br.ifsp.edu.TechSolve.repository.RoleRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User registerUser(UserRegistrationDTO userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }
        if (!userDto.getPassword().equals(userDto.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Password and confirmation do not match");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Role userRole = roleRepository.findByRoleName(ERole.USER)
            .orElseThrow(() -> new RuntimeException("Error: Role USER not found."));
        user.setRoles(Set.of(userRole));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
