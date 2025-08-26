package com.t1.achievements.dto.admin;

public record TrendDeltaDto(
        int currentTotal,
        int previousTotal,
        double percentChange,
        String percentSigned
) {}
