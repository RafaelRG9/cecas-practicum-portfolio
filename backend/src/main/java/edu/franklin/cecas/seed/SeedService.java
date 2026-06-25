package edu.franklin.cecas.seed;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class SeedService {
    private final SeedDataParser seedDataParser;
    private final CategorySeedImporter categorySeedImporter;
    private final ChairSeedImporter chairSeedImporter;
    private final CourseSeedImporter courseSeedImporter;
    private final TransactionTemplate transactionTemplate;

    public SeedService(SeedDataParser seedDataParser, CategorySeedImporter categorySeedImporter, ChairSeedImporter chairSeedImporter, CourseSeedImporter courseSeedImporter, PlatformTransactionManager transactionManager) {
        this.seedDataParser = seedDataParser;
        this.categorySeedImporter = categorySeedImporter;
        this.chairSeedImporter = chairSeedImporter;
        this.courseSeedImporter = courseSeedImporter;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public SeedRunResult seed() {
        ParsedSeedData data = seedDataParser.parseAll();

        return transactionTemplate.execute(status -> {
            CourseSeedImportResult courses = courseSeedImporter.importCourses(data.courses());
            CategorySeedImportResult categories = categorySeedImporter.importCategories(data.categories());
            ChairSeedImportResult chairs = chairSeedImporter.importChairs(data.chairs());
            return new SeedRunResult(courses, categories, chairs);
        });
    }
}
