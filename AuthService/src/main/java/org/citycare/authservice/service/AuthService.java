package org.citycare.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.authservice.dto.AuthResponse;
import org.citycare.authservice.dto.CreateStaffRequest;
import org.citycare.authservice.dto.RegisterRequest;
import org.citycare.authservice.dto.UserProfileUpdateRequest;
import org.citycare.authservice.entity.User;
import org.citycare.authservice.exception.EmailAlreadyRegisteredException;
import org.citycare.authservice.exception.PhoneAlreadyRegisteredException;
import org.citycare.authservice.exception.ResourceNotFoundException;
import org.citycare.authservice.feign.CitizenClient;
import org.citycare.authservice.feign.FacilityClient;
import org.citycare.authservice.feign.dto.CitizenCreateRequest;
import org.citycare.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    // OpenFeign: auto-create citizen profile after citizen registration
    private final CitizenClient citizenClient;
    private final FacilityClient facilityClient;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }
        return doRegister(request);
    }

    @Transactional
    protected AuthResponse doRegister(RegisterRequest request) {

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.CITIZEN)
                .status(User.Status.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // Auto-create citizen profile in citizen-service via OpenFeign
        try {
            CitizenCreateRequest citizenReq = CitizenCreateRequest.builder()
                    .userId(savedUser.getUserId())
                    .name(savedUser.getName())
                    .contactInfo(savedUser.getPhone())
                    .build();
            var citizenResp = citizenClient.createCitizenProfile(citizenReq);
            if (citizenResp != null) {
                log.info("Auto-created citizen profile (citizenId={}) for userId={}",
                        citizenResp.getCitizenId(), savedUser.getUserId());
            }else{
                throw new RuntimeException("User not created with citizen profile");
            }
        } catch (Exception e) {
            // Non-fatal: user is registered; citizen can create profile later
            log.error("Could not auto-create citizen profile for userId={}: {}", savedUser.getUserId(), e.getMessage());
        }

        return mapToResponse(savedUser);
    }

    public User createStaff(CreateStaffRequest req) {
        validateUniqueEmail(req.getEmail());
        return saveUser(req, req.getRole());
    }

    public User createDispatcher(CreateStaffRequest req) {
        validateUniqueEmail(req.getEmail());
        return saveUser(req, User.Role.DISPATCHER);
    }

    public User createComplianceOfficer(CreateStaffRequest req) {
        validateUniqueEmail(req.getEmail());
        return saveUser(req, User.Role.COMPLIANCE_OFFICER);
    }

    @Transactional
    protected User saveUser(CreateStaffRequest req, User.Role role) {
        User saved = userRepository.save(User.builder()
                .name(req.getName()).email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone()).role(role)
                .status(User.Status.ACTIVE).build());
        return saved;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public List<User> getAllStaff() {
        return userRepository.findByRoleIn(List.of(User.Role.DOCTOR));
    }

    public List<User> getAllDoctors() {
        return userRepository.findByRoleIn(List.of(User.Role.DOCTOR));
    }

    public List<User> getAllDispatchers() {
        return userRepository.findByRoleIn(List.of(User.Role.DISPATCHER));
    }

    public List<User> getAllComplianceOfficers() {
        return userRepository.findByRoleIn(List.of(User.Role.COMPLIANCE_OFFICER));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String roleStr) {
        try {
            User.Role role = User.Role.valueOf(roleStr.toUpperCase());
            return userRepository.findByRoleIn(List.of(role));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role requested: {}", roleStr);
            return List.of();
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional
    public User deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        User user = getUserById(id);
        log.info("Found user: {} with current status: {}", user.getEmail(), user.getStatus());
        user.setStatus(User.Status.INACTIVE);
        User saved = userRepository.save(user);
        userRepository.flush();
        syncStaffStatusIfApplicable(saved, "INACTIVE");
        log.info("User {} deactivated. New status: {}", saved.getEmail(), saved.getStatus());
        return saved;
    }

    @Transactional
    public User activateUser(Long id) {
        log.info("Activating user with id: {}", id);
        User user = getUserById(id);
        log.info("Found user: {} with current status: {}", user.getEmail(), user.getStatus());
        user.setStatus(User.Status.ACTIVE);
        User saved = userRepository.save(user);
        userRepository.flush();
        syncStaffStatusIfApplicable(saved, "ACTIVE");
        log.info("User {} activated. New status: {}", saved.getEmail(), saved.getStatus());
        return saved;
    }

    private void syncStaffStatusIfApplicable(User user, String status) {
        if (user.getRole() != User.Role.DOCTOR && user.getRole() != User.Role.DISPATCHER) {
            return;
        }

        try {
            facilityClient.updateStaffStatus(user.getUserId(), status);
            log.info("Synced staff status={} for userId={} role={}", status, user.getUserId(), user.getRole());
        } catch (Exception ex) {
            log.warn("Failed to sync staff status={} for userId={} role={}: {}",
                    status, user.getUserId(), user.getRole(), ex.getMessage());
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return; // already deleted

        try {
            userRepository.delete(user);
            userRepository.flush();
            log.info("User {} ({}) deleted successfully", user.getUserId(), user.getEmail());
        } catch (Exception ex) {
            log.warn("Hard delete failed for user {}. Falling back to deactivate. Cause: {}", id, ex.getMessage());
            User fallbackUser = userRepository.findById(id).orElse(null);
            if (fallbackUser != null) {
                fallbackUser.setStatus(User.Status.INACTIVE);
                userRepository.save(fallbackUser);
                userRepository.flush();
            }
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
    }



    private AuthResponse mapToResponse(User user) {
        return AuthResponse.builder()
                .userId(user.getUserId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole())
                .token(jwtService.generateToken(user))
                .build();
    }


    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate phone uniqueness if phone is being updated
        if (request.getContactInfo() != null && !request.getContactInfo().trim().isEmpty()) {
            // Check if phone is different from current phone
            if (!request.getContactInfo().equals(user.getPhone())) {
                // Check if phone already exists for another user
                if (userRepository.existsByPhone(request.getContactInfo())) {
                    throw new PhoneAlreadyRegisteredException(request.getContactInfo());
                }
            }
        }

        user.setName(request.getName());
        user.setPhone(request.getContactInfo());

        // No need to call save() explicitly
        // JPA will update the entity on transaction commit
    }

}
