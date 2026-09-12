package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.entity.Config;
import com.jyk.wordquiz.wordquiz.repository.ConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConfigService {
    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public Config getConfig() {
        return configRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("Config가 초기화되지 않았습니다."));
    }
}
