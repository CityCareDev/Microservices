package org.citycare.authservice.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: try something different not -> " + email);
    }
}
