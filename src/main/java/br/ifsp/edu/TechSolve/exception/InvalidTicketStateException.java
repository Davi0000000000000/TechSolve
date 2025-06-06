package br.ifsp.edu.TechSolve.exception;

public class InvalidTicketStateException extends RuntimeException {
    public InvalidTicketStateException(String message) {
        super(message);
    }

    public InvalidTicketStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
