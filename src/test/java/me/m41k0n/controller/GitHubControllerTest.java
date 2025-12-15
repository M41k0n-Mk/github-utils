package me.m41k0n.controller;

import me.m41k0n.model.User;
import me.m41k0n.service.ExportService;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.PreviewReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GitHubController.class)
class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @MockBean
    private ExportService exportService;

    @Test
    @DisplayName("GET /api/non-followers/preview returns JSON with totals and users")
    void previewNonFollowersJson() throws Exception {
        var users = List.of(new User("u1", "https://github.com/u1"));
        var report = new PreviewReport(10, 12, 1, users, 1, 25);
        Mockito.when(gitHubService.previewNonFollowers(1,25)).thenReturn(report);
        Mockito.when(gitHubService.isDryRunEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/non-followers/preview").param("page","1").param("size","25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalFollowers").value(10))
                .andExpect(jsonPath("$.totalFollowing").value(12))
                .andExpect(jsonPath("$.totalNonFollowers").value(1))
                .andExpect(jsonPath("$.users[0].login").value("u1"))
                .andExpect(jsonPath("$.dryRunEnabled").value(true));
    }

    @Test
    @DisplayName("GET /api/non-followers/preview?format=csv returns CSV attachment")
    void previewNonFollowersCsvExport() throws Exception {
        var users = List.of(new User("u2", "https://github.com/u2"));
        var report = new PreviewReport(1, 2, 1, users, 2, 25);
        Mockito.when(gitHubService.previewNonFollowers(2,25)).thenReturn(report);
        Mockito.when(exportService.exportToFormat(Mockito.eq(users), Mockito.any())).thenReturn("login,html_url\nu2,https://github.com/u2\n");

        mockMvc.perform(get("/api/non-followers/preview").param("page","2").param("size","25").param("format","csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("non-followers-page-2.csv")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("u2")));
    }
}
