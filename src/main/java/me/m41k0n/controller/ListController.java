package me.m41k0n.controller;

import me.m41k0n.service.ExportService;
import me.m41k0n.service.ListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//TODO lists tb lá no front seta tudo no localstorage, não bate aqui na api do back
@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class ListController {

    private final ListService listService;
    private final ExportService exportService;

    public ListController(ListService listService, ExportService exportService) {
        this.listService = listService;
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(listService.findAllWithCounts());
    }

    public record CreateListRequest(String name, List<String> items) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateListRequest req) {
        var le = listService.create(req.name(), req.items());
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", le.getId());
        resp.put("name", le.getName());
        resp.put("items", req.items() == null ? List.of() : req.items());
        resp.put("createdAt", le.getCreatedAt());
        resp.put("updatedAt", le.getUpdatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        return ResponseEntity.ok(listService.get(id));
    }

    public record UpdateListRequest(String name, List<String> items) {}

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody UpdateListRequest req) {
        var le = listService.update(id, req.name(), req.items());
        return ResponseEntity.ok(listService.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        listService.delete(id);
        return ResponseEntity.noContent().build();
    }
//TODO não entendi muito bem esse apply
    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> apply(@PathVariable String id,
                                                     @RequestParam String action,
                                                     @RequestParam(defaultValue = "true") boolean skipProcessed) {
        return ResponseEntity.ok(listService.apply(id, action, skipProcessed));
    }
    // ===== Export para auditoria/portabilidade =====
    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportOne(@PathVariable String id,
                                       @RequestParam(defaultValue = "csv") String format) {
        try {
            var listData = listService.get(id);
            var exportFormat = ExportService.ExportFormat.fromString(format);
            if (exportFormat == null) {
                return ResponseEntity.badRequest().body("Formato inválido. Use csv ou json.");
            }
            if (exportFormat == ExportService.ExportFormat.CSV) {
                @SuppressWarnings("unchecked")
                var items = (List<String>) listData.getOrDefault("items", List.of());
                String body = exportService.exportListUsernamesToCsv(items);
                String disposition = String.format("attachment; filename=list-%s.csv", id);
                return ResponseEntity.ok()
                        .header("Content-Type", exportFormat.getMimeType())
                        .header("Content-Disposition", disposition)
                        .body(body);
            } else {
                String body = exportService.exportFullListToJson(listData);
                String disposition = String.format("attachment; filename=list-%s.json", id);
                return ResponseEntity.ok()
                        .header("Content-Type", exportFormat.getMimeType())
                        .header("Content-Disposition", disposition)
                        .body(body);
            }
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Erro ao exportar lista: " + ex.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportAll(@RequestParam(defaultValue = "json") String format) {
        try {
            var exportFormat = ExportService.ExportFormat.fromString(format);
            if (exportFormat == null || exportFormat == ExportService.ExportFormat.CSV) {
                return ResponseEntity.badRequest().body("Somente export JSON é suportado para múltiplas listas.");
            }
            var basic = listService.findAllWithCounts();
            var full = new java.util.ArrayList<Map<String, Object>>();
            for (var row : basic) {
                String id = String.valueOf(row.get("id"));
                full.add(listService.get(id));
            }
            String body = exportService.exportAllListsToJson(full);
            return ResponseEntity.ok()
                    .header("Content-Type", exportFormat.getMimeType())
                    .header("Content-Disposition", "attachment; filename=lists.json")
                    .body(body);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Erro ao exportar listas: " + ex.getMessage());
        }
    }
}
