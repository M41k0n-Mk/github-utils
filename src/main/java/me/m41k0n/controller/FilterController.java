package me.m41k0n.controller;

import me.m41k0n.service.ExportService;
import me.m41k0n.service.GitHubInsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/filter")
@CrossOrigin(origins = "*")
public class FilterController {

    private final GitHubInsightsService insightsService;
    private final ExportService exportService;

    public FilterController(GitHubInsightsService insightsService, ExportService exportService) {
        this.insightsService = insightsService;
        this.exportService = exportService;
    }

    @GetMapping("/evaluate")
    public ResponseEntity<?> evaluate(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) Integer inactiveDays,
            @RequestParam(required = false) Integer lastPushDays,
            @RequestParam(required = false) Integer followersLt,
            @RequestParam(required = false) Integer followersGt,
            @RequestParam(required = false) Integer reposLt,
            @RequestParam(required = false) Integer reposGt,
            @RequestParam(required = false) List<String> languages,
            @RequestParam(required = false) Boolean followsYou,
            @RequestParam(required = false) Integer contribLt,
            @RequestParam(required = false) Integer contribGt,
            @RequestParam(required = false) String format
    ) {
        var p = new GitHubInsightsService.FilterParams();
        p.page = page; p.size = size;
        p.inactiveDays = inactiveDays; p.lastPushDays = lastPushDays;
        p.followersLt = followersLt; p.followersGt = followersGt;
        p.reposLt = reposLt; p.reposGt = reposGt;
        p.languages = languages; p.followsYou = followsYou;
        p.contribLt = contribLt; p.contribGt = contribGt;

        var result = insightsService.evaluateFilters(p);

        ExportService.ExportFormat exportFormat = ExportService.ExportFormat.fromString(format);
        if (exportFormat != null) {
            return buildExportResponse(page, format, exportFormat, result);
        }
        return buildEvaluateJsonResponse(result);
    }

    @GetMapping("/smart-suggest")
    public ResponseEntity<Map<String, Object>> smartSuggest(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "25") int size) {
        var result = insightsService.smartSuggest(page, size);
        return buildEvaluateJsonResponse(result);
    }

    private ResponseEntity<?> buildExportResponse(int page, String format,
                                                  ExportService.ExportFormat exportFormat,
                                                  me.m41k0n.service.GitHubInsightsService.PageResult result) {
        String body;
        try {
            if (exportFormat == ExportService.ExportFormat.CSV) {
                body = exportService.exportEnrichedUsersToCsv((java.util.List<?>) result.users);
            } else {
                body = exportService.exportEnrichedUsersToJson((java.util.List<?>) result.users);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao exportar: " + e.getMessage());
        }
        return ResponseEntity.ok()
                .header("Content-Type", exportFormat.getMimeType())
                .header("Content-Disposition", String.format("attachment; filename=filters-page-%d.%s", page, format.toLowerCase()))
                .body(body);
    }

    private ResponseEntity<Map<String, Object>> buildEvaluateJsonResponse(me.m41k0n.service.GitHubInsightsService.PageResult result) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalCandidates", result.totalCandidates);
        resp.put("totalMatched", result.totalMatched);
        resp.put("page", result.page);
        resp.put("size", result.size);
        resp.put("users", result.users);
        return ResponseEntity.ok(resp);
    }
}
