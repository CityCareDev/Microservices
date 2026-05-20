package org.citycare.patienttreatmentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PatientAlreadyAdmittedException extends RuntimeException {
    public PatientAlreadyAdmittedException(String message) {
        super(message);
    }
}
