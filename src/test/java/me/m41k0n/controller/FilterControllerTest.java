package me.m41k0n.controller;

import me.m41k0n.service.ExportService;
import me.m41k0n.service.GitHubInsightsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilterController.class)
class FilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubInsightsService insightsService;

    @MockBean
    private ExportService exportService;

    @Test
    @DisplayName("GET /api/filter/evaluate returns JSON with users and counts")
    void evaluateJson() throws Exception {
        var u = new GitHubInsightsService.EnrichedUser(
                "octo", "https://github.com/octo",
                Instant.parse("2024-01-10T00:00:00Z"),
                Instant.parse("2024-01-11T00:00:00Z"),
                10, 5, Set.of("Java", "Go"), true, true, 7
        );
        var page = new GitHubInsightsService.PageResult(25, 1, 1, 25, List.of(u));
        Mockito.when(insightsService.evaluateFilters(Mockito.any())).thenReturn(page);

        mockMvc.perform(get("/api/filter/evaluate").param("page","1").param("size","25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCandidates").value(25))
                .andExpect(jsonPath("$.totalMatched").value(1))
                .andExpect(jsonPath("$.users[0].login").value("octo"));
    }

    @Test
    @DisplayName("GET /api/filter/evaluate?format=csv returns CSV export")
    void evaluateCsv() throws Exception {
        var u = new GitHubInsightsService.EnrichedUser(
                "dev", "https://github.com/dev",
                Instant.parse("2024-02-10T00:00:00Z"),
                Instant.parse("2024-02-11T00:00:00Z"),
                2, 1, Set.of("Java"), false, true, 3
        );
        var page = new GitHubInsightsService.PageResult(10, 1, 2, 25, List.of(u));
        Mockito.when(insightsService.evaluateFilters(Mockito.any())).thenReturn(page);
        Mockito.when(exportService.exportEnrichedUsersToCsv(Mockito.anyList()))
                .thenReturn("login,html_url\ndev,https://github.com/dev\n");

        mockMvc.perform(get("/api/filter/evaluate").param("page","2").param("size","25").param("format","csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("filters-page-2.csv")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("dev")));
    }

    @Test
    @DisplayName("GET /api/filter/smart-suggest returns preset results")
    void smartSuggest() throws Exception {
        var u = new GitHubInsightsService.EnrichedUser(
                "cold", "https://github.com/cold",
                Instant.parse("2023-01-01T00:00:00Z"),
                Instant.parse("2023-02-01T00:00:00Z"),
                10, 2, Set.of("Python"), false, true, 0
        );
        var page = new GitHubInsightsService.PageResult(30, 1, 1, 25, List.of(u));
        Mockito.when(insightsService.smartSuggest(1,25)).thenReturn(page);

        mockMvc.perform(get("/api/filter/smart-suggest").param("page","1").param("size","25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidates").value(30))
                .andExpect(jsonPath("$.users[0].login").value("cold"));
    }
}
