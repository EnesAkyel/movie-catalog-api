package com.moviecatalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.moviecatalog.model.Studio;
import com.moviecatalog.util.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StudioService {
    private static final Logger logger = LoggerFactory.getLogger(StudioService.class);
    private final List<Studio> studios = new ArrayList<>();

    public StudioService() {
        Random random = new Random();
        String[] studioNames = {"Paramount", "Warner Bros", "Universal", "20th Century", "Miramax"};
        for (int i = 0; i < 5; i++) {
            studios.add(new Studio(random.nextInt(1, 100), studioNames[i]));
        }
    }

    public List<Studio> getAll() {
        return List.copyOf(studios);
    }

    public PageResponse<Studio> getStudios(int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, studios.size());
        List<Studio> content = fromIndex >= studios.size() ? List.of() : studios.subList(fromIndex, toIndex);
        return new PageResponse<>(content, page, size, studios.size());
    }

    public Optional<Studio> add(Studio studio) {
        if (studios.contains(studio)) {
            logger.warn("Studio with SID {} already exists", studio.getSID());
            return Optional.empty();
        }
        studios.add(studio);
        logger.info("Studio created: SID={}", studio.getSID());
        return Optional.of(studio);
    }

    public Optional<Studio> update(int sid, Studio studio) {
        studio.setSID(sid);
        if (!studios.contains(studio)) {
            logger.warn("Studio not found for update: SID={}", sid);
            return Optional.empty();
        }
        Studio existing = studios.get(studios.indexOf(studio));
        existing.setSID(sid);
        existing.setName(studio.getName());
        logger.info("Studio updated: SID={}", sid);
        return Optional.of(existing);
    }

    public Optional<Studio> delete(int sid) {
        Studio temp = new Studio();
        temp.setSID(sid);
        if (!studios.contains(temp)) {
            logger.warn("Studio not found for deletion: SID={}", sid);
            return Optional.empty();
        }
        Studio studio = studios.get(studios.indexOf(temp));
        studios.remove(studio);
        logger.info("Studio deleted: SID={}", sid);
        return Optional.of(studio);
    }
}
