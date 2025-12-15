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

    /**
     * GET /api/lists — lista todas as listas nomeadas com metadados e contagem de itens.
     * Não retorna os itens (usernames) neste endpoint para ser leve.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        return ResponseEntity.ok(listService.findAllWithCounts());
    }

    public record CreateListRequest(String name, List<String> items) {}

    /**
     * POST /api/lists — cria uma lista com nome e itens (usernames).
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateListRequest req) {
        var le = listService.create(req.name(), req.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildListCreateResponse(le, req.items()));
    }

    /**
     * GET /api/lists/{id} — recupera detalhes da lista (inclui usernames em items).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        return ResponseEntity.ok(listService.get(id));
    }

    public record UpdateListRequest(String name, List<String> items) {}

    /**
     * PUT /api/lists/{id} — atualiza nome/itens da lista (sobrescreve itens).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody UpdateListRequest req) {
        listService.update(id, req.name(), req.items());
        return ResponseEntity.ok(listService.get(id));
    }

    /**
     * DELETE /api/lists/{id} — remove lista e seus itens.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        listService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/lists/{id}/apply — aplica follow/unfollow em lote, com opção de pular já processados.
     */
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
            return exportSingleList(id, listData, exportFormat);
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
            var full = loadAllListsFully();
            String body = exportService.exportAllListsToJson(full);
            return ResponseEntity.ok()
                    .header("Content-Type", exportFormat.getMimeType())
                    .header("Content-Disposition", "attachment; filename=lists.json")
                    .body(body);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Erro ao exportar listas: " + ex.getMessage());
        }
    }

    // ===== Helpers privados para legibilidade =====
    private Map<String, Object> buildListCreateResponse(me.m41k0n.entity.ListEntity le, List<String> items) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", le.getId());
        resp.put("name", le.getName());
        resp.put("items", items == null ? List.of() : items);
        resp.put("createdAt", le.getCreatedAt());
        resp.put("updatedAt", le.getUpdatedAt());
        return resp;
    }

    private ResponseEntity<?> exportSingleList(String id, Map<String, Object> listData, ExportService.ExportFormat exportFormat) throws Exception {
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
    }

    private java.util.List<Map<String, Object>> loadAllListsFully() {
        var basic = listService.findAllWithCounts();
        var full = new java.util.ArrayList<Map<String, Object>>();
        for (var row : basic) {
            String id = String.valueOf(row.get("id"));
            full.add(listService.get(id));
        }
        return full;
    }
}
