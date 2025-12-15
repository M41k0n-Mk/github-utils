package me.m41k0n.service;

import me.m41k0n.entity.ListEntity;
import me.m41k0n.entity.ListItemEntity;
import me.m41k0n.repository.ListItemRepository;
import me.m41k0n.repository.ListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExclusionService {

    private static final Logger log = LoggerFactory.getLogger(ExclusionService.class);
    private static final String LIST_NAME = "EXCLUDE_NEXT_RUN";

    private final ListRepository listRepository;
    private final ListItemRepository listItemRepository;

    public ExclusionService(ListRepository listRepository, ListItemRepository listItemRepository) {
        this.listRepository = listRepository;
        this.listItemRepository = listItemRepository;
    }

    private ListEntity getOrCreateList() {
        return listRepository.findByName(LIST_NAME).orElseGet(() -> {
            ListEntity le = new ListEntity();
            le.setName(LIST_NAME);
            le = listRepository.save(le);
            log.info("[EXCLUDE] Lista de exclusões criada: {}", le.getId());
            return le;
        });
    }

    @Transactional
    public int addAll(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) return 0;
        ListEntity list = getOrCreateList();
        Set<String> existing = listItemRepository.findByListId(list.getId()).stream()
                .map(ListItemEntity::getUsername)
                .collect(Collectors.toSet());
        int added = 0;
        for (String u : usernames) {
            if (u == null || u.isBlank() || existing.contains(u)) continue;
            ListItemEntity item = new ListItemEntity();
            item.setListId(list.getId());
            item.setUsername(u);
            item.setList(list);
            listItemRepository.save(item);
            added++;
        }
        if (added > 0) log.info("[EXCLUDE] {} usuários adicionados à lista de exclusões", added);
        return added;
    }

    public Set<String> allUsernames() {
        return listRepository.findByName(LIST_NAME)
                .map(le -> new HashSet<>(listItemRepository.findByListId(le.getId()).stream()
                        .map(ListItemEntity::getUsername)
                        .collect(Collectors.toSet())))
                .orElseGet(HashSet::new);
    }
}
