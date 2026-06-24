package edu.franklin.cecas.seed;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Component
public class ChairSeedFileReader extends AbstractSeedCsvReader<ChairSeedRow> {
    private Set<String> seenEmails = new HashSet<>();

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private record EmailValue(
            @NotBlank @Email String email) {
    }

    @Override
    protected String fileName() {
        return "chairs.csv";
    }

    @Override
    protected List<String> expectedHeaders() {
        return List.of("email", "full_name", "program", "course_codes", "temp_password");
    }

    @Override
    protected ChairSeedRow mapRecord(CSVRecord record, long physicalRowNumber, List<SeedValidationError> errors) {
        // Chairs are matched by normalized email, so store the lowercase form from the
        // start.
        String email = record.get("email").trim().toLowerCase(Locale.ROOT);
        String fullName = record.get("full_name").trim();
        String program = record.get("program").trim();
        Set<String> course_codes = normalizeCourseCodes(record.get("course_codes"));
        String temporaryPassword = record.get("temp_password").trim();

        if (email.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "email", "Email is required."));
            return null;
        }

        if (!isValidEmail(email)) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "email", "Email must be a valid email address."));
            return null;
        }

        if (fullName.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "full_name", "Full name is required."));
            return null;
        }

        if (program.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "program", "Program is required."));
            return null;
        }

        if (course_codes.isEmpty()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "course_codes", "At least one course code is required."));
            return null;
        }

        if (temporaryPassword.isBlank()) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "temp_password", "Temporary password is required."));
            return null;
        }

        if (seenEmails.add(email) == false) {
            errors.add(new SeedValidationError(fileName(), physicalRowNumber,
                    "email", "Duplicate email found after normalization."));
            return null;
        }

        return new ChairSeedRow(email, fullName, program, course_codes, temporaryPassword);
    }

    // Reset per-file duplicate tracking because this reader bean is reused across
    // seed runs.
    @Override
    public List<ChairSeedRow> read(Path file) {
        seenEmails = new HashSet<>();
        return super.read(file);
    }

    // Normalize the pipe-delimited course list to match seed rules: trim,
    // uppercase,
    // drop blanks, and remove duplicates while preserving CSV order.
    private Set<String> normalizeCourseCodes(String rawCourseCodes) {
        if (rawCourseCodes == null) {
            return new LinkedHashSet<>();
        }

        return Arrays.stream(rawCourseCodes.split("\\|"))
                .map(String::trim)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .filter(code -> !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isValidEmail(String email) {
        return VALIDATOR.validate(new EmailValue(email)).isEmpty();
    }
}
