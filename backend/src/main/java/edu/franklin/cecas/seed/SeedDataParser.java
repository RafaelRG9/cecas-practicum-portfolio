package edu.franklin.cecas.seed;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import edu.franklin.cecas.config.SeedProperties;
import edu.franklin.cecas.exception.SeedValidationException;

@Component
public class SeedDataParser {
    private final SeedProperties seedProperties;
    private final CourseSeedFileReader courseSeedFileReader;
    private final CategorySeedFileReader categorySeedFileReader;
    private final ChairSeedFileReader chairSeedFileReader;

    public SeedDataParser(SeedProperties seedProperties, CourseSeedFileReader courseSeedFileReader,
            CategorySeedFileReader categorySeedFileReader, ChairSeedFileReader chairSeedFileReader) {
        this.seedProperties = seedProperties;
        this.courseSeedFileReader = courseSeedFileReader;
        this.categorySeedFileReader = categorySeedFileReader;
        this.chairSeedFileReader = chairSeedFileReader;
    }

    public ParsedSeedData parseAll() {
        Path seedPath = seedProperties.path();
        Path coursesPath = seedPath.resolve("courses.csv");
        Path chairsPath = seedPath.resolve("chairs.csv");
        Path categoriesPath = seedPath.resolve("categories.csv");

        List<CourseSeedRow> courses = null;
        List<ChairSeedRow> chairs = null;
        List<CategorySeedRow> categories = null;
        List<SeedValidationError> errors = new ArrayList<>();

        try {
            courses = courseSeedFileReader.read(coursesPath);
        } catch (SeedValidationException ex) {
            errors.addAll(ex.getErrors());
        }

        try {
            chairs = chairSeedFileReader.read(chairsPath);
        } catch (SeedValidationException ex) {
            errors.addAll(ex.getErrors());
        }

        try {
            categories = categorySeedFileReader.read(categoriesPath);
        } catch (SeedValidationException ex) {
            errors.addAll(ex.getErrors());
        }

        if (courses != null && chairs != null) {
            Set<String> knownCourseCodes = courses.stream()
                    .map(CourseSeedRow::courseCode)
                    .collect(Collectors.toSet());

            for (int i = 0; i < chairs.size(); i++) {
                ChairSeedRow chair = chairs.get(i);
                long rowNumber = i + 2;

                for (String courseCode : chair.courseCodes()) {
                    if (!knownCourseCodes.contains(courseCode)) {
                        errors.add(new SeedValidationError("chairs.csv", rowNumber,
                                "course_codes", "Unknown course code '" + courseCode + "'."));
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new SeedValidationException(errors);
        }

        return new ParsedSeedData(courses, chairs, categories);
    }
}
