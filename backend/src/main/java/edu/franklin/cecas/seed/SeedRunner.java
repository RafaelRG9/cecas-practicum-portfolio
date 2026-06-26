package edu.franklin.cecas.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.franklin.cecas.exception.SeedSynchronizationException;
import edu.franklin.cecas.exception.SeedValidationException;

@Component
@Profile("!seed")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@DependsOnDatabaseInitialization
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);
    private final SeedService seedService;

    public SeedRunner(SeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            SeedRunResult result = seedService.seed();
            log.info("Startup seed completed successfully: {}", result.toLogSummary());
        } catch (SeedValidationException | SeedSynchronizationException ex) {
            log.error("Startup seed failed: {}", ex.getMessage());
            throw new IllegalStateException("Startup seed failed", ex);
        }
    }
}
