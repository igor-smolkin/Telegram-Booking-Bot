package com.ataraxii.testspringbot.db.repository;

import com.ataraxii.testspringbot.db.entity.BotSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotSettingsRepository extends JpaRepository<BotSettings, Long> {
}
