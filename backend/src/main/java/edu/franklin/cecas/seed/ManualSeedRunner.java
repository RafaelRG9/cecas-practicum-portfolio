package edu.franklin.cecas.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.franklin.cecas.exception.SeedSynchronizationException;
import edu.franklin.cecas.exception.SeedValidationException;

@Component
@Profile("seed")
@DependsOnDatabaseInitialization
public class ManualSeedRunner implements ApplicationRunner {
    
    private static final Logger log = LoggerFactory.getLogger(ManualSeedRunner.class);
    private final SeedService seedService;

    public ManualSeedRunner(SeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            SeedRunResult result = seedService.seed();
            log.info("Manual seed completed successfully: {}", result.toLogSummary());
        } catch (SeedValidationException | SeedSynchronizationException ex) {
            log.error("Manual seed failed: {}", ex.getMessage());
            throw new IllegalStateException("Manual seed failed", ex);
        }
    }
}
