package com.t1.achievements.RR;

import java.util.List;

public record AuthResponse(
        String token,
        List<String> roles
) {
}
