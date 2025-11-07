package me.m41k0n.controller;

import me.m41k0n.service.ListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class ListController {

    private final ListService listService;

    public ListController(ListService listService) {
        this.listService = listService;
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

    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> apply(@PathVariable String id,
                                                     @RequestParam String action,
                                                     @RequestParam(defaultValue = "true") boolean skipProcessed) {
        return ResponseEntity.ok(listService.apply(id, action, skipProcessed));
    }
}
