package com.devphilip.userservice.controllers;


import com.devphilip.userservice.dto.*;
import com.devphilip.userservice.entities.ERole;
import com.devphilip.userservice.entities.RefreshToken;
import com.devphilip.userservice.entities.Role;
import com.devphilip.userservice.entities.User;
import com.devphilip.userservice.exceptions.TokenRefreshException;
import com.devphilip.userservice.repositories.RoleRepository;
import com.devphilip.userservice.repositories.UserRepository;
import com.devphilip.userservice.security.UserDetailsImpl;
import com.devphilip.userservice.security.jwt.JwtUtils;
import com.devphilip.userservice.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "All Users Login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), roles));

    }

    @Operation(summary = "User request refresh token")
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @Operation(summary = "User Signup or Admin Add New User")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signupRequest.getFirstName(), signupRequest.getLastName(), signupRequest.getEmail(), encoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add(getRoleFromDB(ERole.ROLE_USER));
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        roles.add(getRoleFromDB(ERole.ROLE_ADMIN));
                        break;
                    case "mod":
                        roles.add(getRoleFromDB(ERole.ROLE_MODERATOR));
                        break;
                    default:
                        roles.add(getRoleFromDB(ERole.ROLE_USER));
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Operation(summary = "Enable User's account")
    @PostMapping("/enable-user")
    public ResponseEntity<?> enableUser(@Valid @RequestBody ActivateUserRequest userRequest) {

        if (!userRepository.existsByEmail(userRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email not present!"));
        }
        User user = userRepository.findByEmail(userRequest.getEmail()).get();
        user.setActive(true);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User activated successfully!"));
    }

    private Role getRoleFromDB(ERole roleUser) {
        return roleRepository.findByName(roleUser)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    }
}
