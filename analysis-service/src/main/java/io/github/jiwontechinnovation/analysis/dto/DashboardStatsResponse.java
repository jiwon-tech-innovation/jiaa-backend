package io.github.jiwontechinnovation.analysis.dto;

import java.util.List;

public record DashboardStatsResponse(
        List<RadarStatDto> radarData,
        int currentStreak,
        int completedDays,
        int totalDays,
        int completedItems,
        List<List<Integer>> contributionData) {
}
