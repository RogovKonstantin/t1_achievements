package com.t1.achievements.controller;

import com.t1.achievements.RR.AuthResponse;
import com.t1.achievements.RR.LoginRequest;
import com.t1.achievements.repository.UserRepository;
import com.t1.achievements.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService uds;
    private final JwtService jwt;
    private final UserRepository userRepo;

    @Operation(summary = "Вход по логину/паролю", description = "Возвращает JWT с ролями в claim `roles`")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        var auth = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        authManager.authenticate(auth);

        UserDetails user = uds.loadUserByUsername(req.username());

        List<String> roles = user.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList();

        var domainUser = userRepo.findByUsername(req.username()).orElseThrow();
        Map<String,Object> extra = Map.of(
                "uid", domainUser.getId().toString(),
                "fullName", domainUser.getFullName()
        );

        String token = jwt.generateToken(user, roles, extra);
        return ResponseEntity.ok(new AuthResponse(token, roles));
    }
}

