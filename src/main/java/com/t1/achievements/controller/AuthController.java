package com.t1.achievements.controller;

import com.t1.achievements.RR.AuthResponse;
import com.t1.achievements.RR.LoginRequest;
import com.t1.achievements.repository.UserRepository;
import com.t1.achievements.security.JwtService;
import com.t1.achievements.service.AssetStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

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
    private final AssetStorageService assets;

    @Operation(summary = "Вход по логину/паролю", description = "Возвращает JWT с ролями в claim `role`")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDetails principal = (UserDetails) auth.getPrincipal();

        String role = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .min((a, b) -> a.equals("ADMIN") ? -1 : 1)
                .orElse("USER");

        var u = userRepo.findByUsername(principal.getUsername()).orElseThrow();

        String avatarUrl = (u.getAvatar() != null) ? assets.publicUrl(u.getAvatar()) : null;

        Map<String, Object> extra = Map.of(
                "id", u.getId().toString(),
                "fullname", u.getFullName(),
                "avatarUrl", avatarUrl
        );

        String token = jwt.generateToken(principal, role, extra);
        return ResponseEntity.ok(new AuthResponse(token, role));
    }
}
