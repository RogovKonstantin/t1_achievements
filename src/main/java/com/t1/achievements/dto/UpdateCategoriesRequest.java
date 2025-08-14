package com.t1.achievements.dto;

import java.util.Set;
import java.util.UUID;

public record UpdateCategoriesRequest(Set<UUID> categoryIds) {
}
