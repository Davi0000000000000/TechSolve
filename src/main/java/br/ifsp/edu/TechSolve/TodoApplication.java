package br.ifsp.edu.TechSolve;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.ifsp.edu.TechSolve.repository.RoleRepository;
import br.ifsp.edu.TechSolve.repository.UserRepository;

@SpringBootApplication
public class TodoApplication {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}