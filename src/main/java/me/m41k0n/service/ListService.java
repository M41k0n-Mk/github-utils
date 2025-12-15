package me.m41k0n.service;

import me.m41k0n.entity.ListEntity;
import me.m41k0n.entity.ListItemEntity;
import me.m41k0n.repository.ListItemRepository;
import me.m41k0n.repository.ListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ListService {

    private final ListRepository listRepository;
    private final ListItemRepository listItemRepository;
    private final HistoryService historyService;
    private final GitHubService gitHubService;

    public ListService(ListRepository listRepository, ListItemRepository listItemRepository, HistoryService historyService, GitHubService gitHubService) {
        this.listRepository = listRepository;
        this.listItemRepository = listItemRepository;
        this.historyService = historyService;
        this.gitHubService = gitHubService;
    }

    public List<Map<String, Object>> findAllWithCounts() {
        List<ListEntity> lists = listRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ListEntity le : lists) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", le.getId());
            row.put("name", le.getName());
            row.put("createdAt", le.getCreatedAt());
            row.put("updatedAt", le.getUpdatedAt());
            row.put("count", listItemRepository.findByListId(le.getId()).size());
            result.add(row);
        }
        return result;
    }

    @Transactional
    public ListEntity create(String name, List<String> items) {
        ListEntity le = new ListEntity();
        le.setName(name);
        le = listRepository.save(le);
        if (items != null) {
            for (String u : items) {
                ListItemEntity item = new ListItemEntity();
                item.setListId(le.getId());
                item.setUsername(u);
                item.setList(le);
                listItemRepository.save(item);
            }
        }
        return le;
    }

    public Map<String, Object> get(String id) {
        ListEntity le = listRepository.findById(id).orElseThrow(() -> new NoSuchElementException("List not found"));
        List<ListItemEntity> items = listItemRepository.findByListId(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", le.getId());
        resp.put("name", le.getName());
        resp.put("createdAt", le.getCreatedAt());
        resp.put("updatedAt", le.getUpdatedAt());
        resp.put("items", items.stream().map(ListItemEntity::getUsername).toList());
        return resp;
    }

    @Transactional
    public ListEntity update(String id, String name, List<String> items) {
        ListEntity le = listRepository.findById(id).orElseThrow(() -> new NoSuchElementException("List not found"));
        if (name != null && !name.isBlank()) {
            le.setName(name);
        }
        if (items != null) {
            listItemRepository.deleteByListId(id);
            for (String u : items) {
                ListItemEntity item = new ListItemEntity();
                item.setListId(id);
                item.setUsername(u);
                item.setList(le);
                listItemRepository.save(item);
            }
        }
        return listRepository.save(le);
    }

    @Transactional
    public void delete(String id) {
        ListEntity le = listRepository.findById(id).orElseThrow(() -> new NoSuchElementException("List not found"));
        listRepository.delete(le);
    }

    public Map<String, Object> apply(String id, String action, boolean skipProcessed) {
        ListEntity le = listRepository.findById(id).orElseThrow(() -> new NoSuchElementException("List not found"));
        List<ListItemEntity> items = listItemRepository.findByListId(id);
        int applied = 0;
        int skipped = 0;
        boolean dryRun = false;
        List<Map<String, Object>> details = new ArrayList<>();

        for (ListItemEntity item : items) {
            var result = processListItem(action, skipProcessed, le, item);
            if (result.skipped) {
                skipped++;
            } else {
                applied++;
                dryRun = dryRun || result.opDry;
            }
            details.add(result.detail);
        }
        return buildApplyResponse(applied, skipped, details, dryRun);
    }

    private Map<String, Object> buildApplyResponse(int applied, int skipped, List<Map<String, Object>> details, boolean dryRun) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("applied", applied);
        resp.put("skipped", skipped);
        resp.put("details", details);
        resp.put("dryRun", dryRun);
        return resp;
    }

    private record ItemResult(boolean skipped, boolean opDry, Map<String, Object> detail) {}

    private ItemResult processListItem(String action, boolean skipProcessed, ListEntity le, ListItemEntity item) {
        String username = item.getUsername();
        Map<String, Object> d = new HashMap<>();
        d.put("username", username);
        d.put("action", action);

        if (skipProcessed && historyService.alreadyProcessed(username, action)) {
            d.put("skippedReason", "already-" + action);
            return new ItemResult(true, false, d);
        }

        boolean opDry;
        if ("unfollow".equalsIgnoreCase(action)) {
            opDry = gitHubService.unfollow(username, le.getId());
        } else if ("follow".equalsIgnoreCase(action)) {
            opDry = gitHubService.follow(username, le.getId());
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }
        return new ItemResult(false, opDry, d);
    }
}
