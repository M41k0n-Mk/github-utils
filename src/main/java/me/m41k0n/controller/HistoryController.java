package me.m41k0n.controller;

import me.m41k0n.entity.HistoryEntity;
import me.m41k0n.service.HistoryService;
import me.m41k0n.service.ExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    private final HistoryService historyService;
    private final ExportService exportService;

    public HistoryController(HistoryService historyService, ExportService exportService) {
        this.historyService = historyService;
        this.exportService = exportService;
    }
//history lá no frontend parece que pega do cache não bate no endpoint aqui
    /**
     * GET /api/history — consulta o histórico de ações follow/unfollow.
     *
     * Filtros opcionais via query string:
     * - username: login a filtrar
     * - action: "follow" | "unfollow"
     * - since: instante ISO-8601 (inclui eventos a partir deste instante)
     *
     * Resposta 200: array de HistoryEntity com campos { id, username, action, timestamp, sourceListId, dryRun }.
     */
    @GetMapping
    public ResponseEntity<List<HistoryEntity>> getHistory(@RequestParam(required = false) String username,
                                                          @RequestParam(required = false) String action,
                                                          @RequestParam(required = false) String since) {
        Instant sinceInstant = parseInstantOrNull(since);
        return ResponseEntity.ok(historyService.search(username, action, sinceInstant));
    }

    /**
     * GET /api/history/export — exporta o histórico para CSV ou JSON conforme filtros.
     *
     * Filtros opcionais via query string:
     * - username, action (follow|unfollow), since (ISO-8601)
     * - format: csv | json (obrigatório aqui; default csv)
     *
     * Resposta 200: arquivo (attachment) com Content-Type e Content-Disposition apropriados.
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportHistory(@RequestParam(required = false) String username,
                                           @RequestParam(required = false) String action,
                                           @RequestParam(required = false) String since,
                                           @RequestParam(defaultValue = "csv") String format) {
        try {
            Instant sinceInstant = parseInstantOrNull(since);
            List<HistoryEntity> items = historyService.search(username, action, sinceInstant);

            var exportFormat = me.m41k0n.service.ExportService.ExportFormat.fromString(format);
            if (exportFormat == null) {
                return ResponseEntity.badRequest().body("Formato inválido. Use csv ou json.");
            }

            return buildExportHistoryResponse(items, exportFormat, action, format);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Erro ao exportar histórico: " + ex.getMessage());
        }
    }

    private Instant parseInstantOrNull(String iso) {
        return (iso != null && !iso.isBlank()) ? Instant.parse(iso) : null;
    }

    private ResponseEntity<?> buildExportHistoryResponse(List<HistoryEntity> items,
                                                         me.m41k0n.service.ExportService.ExportFormat exportFormat,
                                                         String action,
                                                         String format) throws Exception {
        String body = exportService.exportHistoryToFormat(items, exportFormat);
        String disposition = String.format("attachment; filename=history-%s.%s",
                action == null ? "all" : action,
                format.toLowerCase());
        return ResponseEntity.ok()
                .header("Content-Type", exportFormat.getMimeType())
                .header("Content-Disposition", disposition)
                .body(body);
    }
}
