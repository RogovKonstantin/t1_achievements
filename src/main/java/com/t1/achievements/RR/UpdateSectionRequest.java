package com.t1.achievements.RR;

import java.util.Optional;

public record UpdateSectionRequest(
        Optional<String> name,
        Optional<String> description
) {}
