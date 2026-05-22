package org.citycare.facilityservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.citycare.facilityservice.dto.request.StaffRequest;
import org.citycare.facilityservice.dto.response.StaffResponse;
import org.citycare.facilityservice.dto.response.ApiResponse;
import org.citycare.facilityservice.entities.Staff;
import org.citycare.facilityservice.services.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.citycare.facilityservice.exceptions.ResourceNotFoundException;
import java.util.List;
// --- Exception Handling for Validation and Not Found ---
@RestControllerAdvice
class StaffControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(StaffControllerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleOther(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected system error occurred."));
    }
}

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> create(@Valid @RequestBody StaffRequest request) {
        StaffResponse data = staffService.createStaff(request);
        return new ResponseEntity<>(ApiResponse.ok("Staff account created successfully", data), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<StaffResponse>> getById(@PathVariable Long id) {
        StaffResponse data = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.ok("Staff details retrieved", data));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAll() {
        List<StaffResponse> data = staffService.getAllStaff();
        return ResponseEntity.ok(ApiResponse.ok("All staff members retrieved", data));
    }

    @GetMapping("/facility/{facilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getByFacility(@PathVariable Long facilityId) {
        List<StaffResponse> data = staffService.getStaffByFacility(facilityId);
        return ResponseEntity.ok(ApiResponse.ok("Staff members for facility retrieved", data));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getByRole(@PathVariable Staff.Role role) {
        List<StaffResponse> data = staffService.getStaffByRole(role);
        return ResponseEntity.ok(ApiResponse.ok("Staff members filtered by role: " + role, data));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Staff.Status status) {
        StaffResponse data = staffService.updateStaffStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Staff status updated to " + status, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.ok("Staff member deleted successfully", null));
    }
}