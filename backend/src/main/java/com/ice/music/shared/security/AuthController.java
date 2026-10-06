package com.ice.music.shared.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for obtaining JWT authentication tokens")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final String adminUsername;
    private final String adminPassword;

    public AuthController(
        JwtUtil jwtUtil,
        @Value("${app.security.admin.username:admin}") String adminUsername,
        @Value("${app.security.admin.password:password123}") String adminPassword
    ) {
        this.jwtUtil = jwtUtil;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String token, String username, List<String> roles, long expiresIn) {}

    @PostMapping("/token")
    @Operation(summary = "Authenticate and receive a JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (adminUsername.equals(request.username()) && adminPassword.equals(request.password())) {
            List<String> roles = List.of("ROLE_EDITOR", "ROLE_ADMIN");
            String token = jwtUtil.generateToken(request.username(), roles);
            return ResponseEntity.ok(new TokenResponse(token, request.username(), roles, 86400000));
        }
        return ResponseEntity.status(401).body("Invalid username or password. Default admin: admin / password123");
    }

    @PostMapping("/editor-token")
    @Operation(summary = "One-click test token generator for reviewers & frontend demo")
    public ResponseEntity<TokenResponse> getEditorToken() {
        List<String> roles = List.of("ROLE_EDITOR");
        String token = jwtUtil.generateToken("editor@ice.services", roles);
        return ResponseEntity.ok(new TokenResponse(token, "editor@ice.services", roles, 86400000));
    }
}
