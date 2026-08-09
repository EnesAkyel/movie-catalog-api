package com.moviecatalog.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final String username;
    private final String password;
    private final JwtUtil jwtUtil;

    public AuthController(
            @Value("${app.auth.username}") String username,
            @Value("${app.auth.password}") String password,
            JwtUtil jwtUtil) {
        this.username = username;
        this.password = password;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        if (username.equals(request.username()) && password.equals(request.password())) {
            return ResponseEntity.ok(new TokenResponse(jwtUtil.generateToken(request.username())));
        }
        return ResponseEntity.status(401).build();
    }
}
