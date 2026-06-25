package com.moviecatalog.service;

import com.moviecatalog.model.Studio;
import com.moviecatalog.repository.StudioRepository;
import com.moviecatalog.util.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudioService {
    private static final Logger logger = LoggerFactory.getLogger(StudioService.class);
    private final StudioRepository studioRepository;

    public StudioService(StudioRepository studioRepository) {
        this.studioRepository = studioRepository;
    }

    public List<Studio> getAll() {
        return studioRepository.findAll();
    }

    public PageResponse<Studio> getStudios(int page, int size) {
        if (size <= 0) {
            return new PageResponse<>(List.of(), page, 0, studioRepository.count());
        }
        Page<Studio> result = studioRepository.findAll(PageRequest.of(page, size));
        return new PageResponse<>(result.getContent(), page, size, result.getTotalElements());
    }

    public Optional<Studio> add(Studio studio) {
        if (studioRepository.existsById(studio.getSID())) {
            logger.warn("Studio with SID {} already exists", studio.getSID());
            return Optional.empty();
        }
        logger.info("Studio created: SID={}", studio.getSID());
        return Optional.of(studioRepository.save(studio));
    }

    public Optional<Studio> update(int sid, Studio studio) {
        Optional<Studio> found = studioRepository.findById(sid);
        if (found.isEmpty()) {
            logger.warn("Studio not found for update: SID={}", sid);
            return Optional.empty();
        }
        Studio existing = found.get();
        existing.setName(studio.getName());
        logger.info("Studio updated: SID={}", sid);
        return Optional.of(studioRepository.save(existing));
    }

    public Optional<Studio> delete(int sid) {
        Optional<Studio> found = studioRepository.findById(sid);
        if (found.isEmpty()) {
            logger.warn("Studio not found for deletion: SID={}", sid);
            return Optional.empty();
        }
        Studio studio = found.get();
        studioRepository.delete(studio);
        logger.info("Studio deleted: SID={}", sid);
        return Optional.of(studio);
    }
}
