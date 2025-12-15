package me.m41k0n.service;

import me.m41k0n.entity.ListEntity;
import me.m41k0n.entity.ListItemEntity;
import me.m41k0n.repository.HistoryRepository;
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
            List<ListItemEntity> itemEntities = new ArrayList<>();
            for (String u : items) {
                ListItemEntity item = new ListItemEntity();
                item.setListId(le.getId());
                item.setUsername(u);
                item.setList(le);
                itemEntities.add(item);
            }
            listItemRepository.saveAll(itemEntities);
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
            List<ListItemEntity> itemEntities = new ArrayList<>();
            for (String u : items) {
                ListItemEntity item = new ListItemEntity();
                item.setListId(id);
                item.setUsername(u);
                item.setList(le);
                itemEntities.add(item);
            }
            listItemRepository.saveAll(itemEntities);
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
            String username = item.getUsername();
            Map<String, Object> d = new HashMap<>();
            d.put("username", username);
            d.put("action", action);
            if (skipProcessed && historyService.alreadyProcessed(username, action)) {
                d.put("skippedReason", "already-" + action);
                skipped++;
                details.add(d);
                continue;
            }
            boolean opDry;
            if ("unfollow".equalsIgnoreCase(action)) {
                opDry = gitHubService.unfollow(username, le.getId());
            } else if ("follow".equalsIgnoreCase(action)) {
                opDry = gitHubService.follow(username, le.getId());
            } else {
                throw new IllegalArgumentException("Invalid action: " + action);
            }
            dryRun = dryRun || opDry;
            applied++;
            details.add(d);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("applied", applied);
        resp.put("skipped", skipped);
        resp.put("details", details);
        resp.put("dryRun", dryRun);
        return resp;
    }
}
