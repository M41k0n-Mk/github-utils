package me.m41k0n.service;

import me.m41k0n.entity.HistoryEntity;
import me.m41k0n.repository.HistoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void record(String username, String action, boolean dryRun, String sourceListId) {
        HistoryEntity h = new HistoryEntity();
        h.setUsername(username);
        h.setAction(action);
        h.setSourceListId(sourceListId);
        h.setDryRun(dryRun ? 1 : 0);
        // timestamp created at persist if null
        historyRepository.save(h);
    }

    public List<HistoryEntity> search(String username, String action, Instant since) {
        String sinceIso = since != null ? since.toString() : null;
        return historyRepository.search(username, action, sinceIso);
    }

    public boolean alreadyProcessed(String username, String action) {
        return historyRepository.existsByUsernameAndActionAndDryRun(username, action, 0);
    }

    public List<HistoryEntity> findUnfollowsSince(Instant since) {
        return historyRepository.findByActionAndDryRunAndTimestampGreaterThanEqual("unfollow", 0, since.toString());
    }
}