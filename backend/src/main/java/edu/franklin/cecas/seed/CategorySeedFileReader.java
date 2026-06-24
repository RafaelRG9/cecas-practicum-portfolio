package edu.franklin.cecas.seed;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CategorySeedFileReader extends AbstractSeedCsvReader<CategorySeedRow> {
    private Set<String> seenCategoryNames = new HashSet<>();

    @Override
    protected String fileName() {
        return "categories.csv";
    }

    @Override
    protected List<String> expectedHeaders() {
        return List.of("category_name", "description", "default_points");
    }

    @Override
    protected CategorySeedRow mapRecord(CSVRecord record, long physicalRowNumber, List<SeedValidationError> errors) {
        String categoryName = record.get("category_name").trim();
        String description = record.get("description").trim();
        String defaultPointsRaw = record.get("default_points").trim();
        int defaultPoints;

        if (categoryName.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "category_name", "Category name is required."));
            return null;
        }

        if (description.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "description", "Description is required."));
            return null;
        }

        if (defaultPointsRaw.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "default_points", "Default points is required."));
            return null;
        }

        try {
            defaultPoints = Integer.parseInt(defaultPointsRaw);

            if (defaultPoints < 0) {
                errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                        "default_points", "Default points must be greater than or equal to 0."));
                return null;
            }

        } catch (NumberFormatException e) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "default_points", "Default points must be a valid integer."));
            return null;
        }

        // Normalize to a case-insensitive key so "Labs" and "labs" count as the same
        // category.
        String key = categoryName.toLowerCase(Locale.ROOT);
        if (seenCategoryNames.add(key) == false) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "category_name", "Duplicate category row found after normalization."));
            return null;
        }

        return new CategorySeedRow(categoryName, description, defaultPoints);
    }

    // Reset per-file duplicate tracking because this reader bean is reused across
    // seed runs.
    @Override
    public List<CategorySeedRow> read(Path file) {
        seenCategoryNames = new HashSet<>();
        return super.read(file);
    }
}
