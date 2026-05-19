package org.citycare.authservice.security;

import lombok.RequiredArgsConstructor;
import org.citycare.authservice.entity.User;
import org.citycare.authservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        boolean enabled = user.getStatus() == User.Status.ACTIVE;
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(),
            enabled,
            true,
            true,
            true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
