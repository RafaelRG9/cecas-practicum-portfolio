package edu.franklin.cecas.seed;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CourseSeedFileReader extends AbstractSeedCsvReader<CourseSeedRow> {
    private Set<String> seenCourseKeys = new HashSet<>();

    @Override
    protected String fileName() {
        return "courses.csv";
    }

    @Override
    protected List<String> expectedHeaders() {
        return List.of("course_code", "term", "section");
    }

    @Override
    protected CourseSeedRow mapRecord(CSVRecord record, long physicalRowNumber, List<SeedValidationError> errors) {
        String courseCode = record.get("course_code").trim().toUpperCase(Locale.ROOT);
        String term = record.get("term").trim().toUpperCase(Locale.ROOT);
        String section = record.get("section").trim().toUpperCase(Locale.ROOT);

        if (courseCode.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "course_code", "Course code is required."));
            return null;
        }

        if (term.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "term", "Term is required."));
            return null;
        }

        if (section.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "section", "Section is required."));
            return null;
        }

        String key = courseCode + "|" + term + "|" + section;
        if (seenCourseKeys.add(key) == false) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "course_key", "Duplicate course row found after normalization."));
            return null;
        }

        return new CourseSeedRow(courseCode, term, section);
    }

    @Override
    public List<CourseSeedRow> read(Path file) {
        seenCourseKeys = new HashSet<>();
        return super.read(file);
    }
}
