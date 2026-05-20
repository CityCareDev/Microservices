package org.citycare.emergencyservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) { super(msg); }
    public ResourceNotFoundException(String res, Long id) { super(res + " not found with id: " + id); }
}

