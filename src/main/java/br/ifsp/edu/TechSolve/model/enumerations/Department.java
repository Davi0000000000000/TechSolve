package br.ifsp.edu.TechSolve.model.enumerations;

import java.util.Arrays;

public enum Department {
    IT,              
    HR,              
    FINANCE,         
    OPERATIONS,      
    LOGISTICS,       
    ADMINISTRATION,  
    CUSTOMER_SERVICE, 
    MAINTENANCE,      
    OTHER;            

    public static Department fromString(String value) {
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid department: " + value));
    }
}
