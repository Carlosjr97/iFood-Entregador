package com.biometric.capture.controller;

import com.biometric.capture.dto.DashboardStatsDto;
import com.biometric.capture.dto.RankingEntryDto;
import com.biometric.capture.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardStatsDto stats() {
        return dashboardService.getStats();
    }

    @GetMapping("/ranking")
    public List<RankingEntryDto> ranking() {
        return dashboardService.getRanking();
    }
}
