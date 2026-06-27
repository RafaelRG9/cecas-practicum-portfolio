package edu.franklin.cecas.seed;

import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CourseSeedFileReader extends AbstractSeedCsvReader<CourseSeedRow> {
    // These patterns are applied after trim + uppercase normalization,
    // so they define the accepted seed formats.
    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]+-\\d{3}$");
    private static final Pattern TERM_PATTERN = Pattern.compile("^\\d{2}/(FA|SP|SU)$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^[A-Z0-9]{4}$");

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

        if (!COURSE_CODE_PATTERN.matcher(courseCode).matches()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "course_code", "Course code must match DEPT-123 format."));
            return null;
        }

        if (!TERM_PATTERN.matcher(term).matches()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "term", "Term must match YY/FA, YY/SP, or YY/SU format."));
            return null;
        }

        if (!SECTION_PATTERN.matcher(section).matches()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "section", "Section must be exactly four uppercase alphanumeric characters."));
            return null;
        }

        // Duplicate detection uses the normalized natural key, so rows that differ
        // only by casing or surrounding whitespace are treated as the same course.
        // Courses are unique by normalized course_code + term + section.
        String key = courseCode + "|" + term + "|" + section;
        if (seenCourseKeys.add(key) == false) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "course_key", "Duplicate course row found after normalization."));
            return null;
        }

        return new CourseSeedRow(courseCode, term, section);
    }

    // Reset per-file duplicate tracking because this reader bean is reused across
    // seed runs.
    @Override
    public List<CourseSeedRow> read(Path file) {
        seenCourseKeys = new HashSet<>();
        return super.read(file);
    }
}
