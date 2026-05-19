package org.citycare.authservice.exception;

public class PhoneAlreadyRegisteredException extends RuntimeException {
    public PhoneAlreadyRegisteredException(String phone) {
        super("Phone number already registered: " + phone);
    }
}
