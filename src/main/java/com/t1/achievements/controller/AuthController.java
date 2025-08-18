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
import org.springframework.security.core.GrantedAuthority;
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

    @Operation(summary = "Вход по логину/паролю", description = "Возвращает JWT с ролями в claim `role`")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDetails user = (UserDetails) auth.getPrincipal();

        String role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5)).min((a, b) -> a.equals("ADMIN") ? -1 : 1)
                .orElse("USER");

        var u = userRepo.findByUsername(user.getUsername()).orElseThrow();

        Map<String, Object> extra = Map.of(
                "id", u.getId().toString(),
                "fullname", u.getFullName(),
                "avatar", u.getAvatar() != null ? u.getAvatar().getId().toString() : null
        );

        String token = jwt.generateToken(user, role, extra);
        return ResponseEntity.ok(new AuthResponse(token,role));
    }
}

