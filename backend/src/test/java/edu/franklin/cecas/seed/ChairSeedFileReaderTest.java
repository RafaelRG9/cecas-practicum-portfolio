package edu.franklin.cecas.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.franklin.cecas.exception.SeedValidationException;

public class ChairSeedFileReaderTest {

    @TempDir
    Path tempDir;

    private final ChairSeedFileReader reader = new ChairSeedFileReader();

    private Path writeCSV(String fileName, String contents) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, contents);
        return file;
    }

    /**
     * Verifies that chair rows are normalized correctly.
     * @throws IOException
     */
    @Test
    void testReadNormalizesValidChairRows() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                 Grace.Hopper@Email.Franklin.edu , Grace Hopper , Computer Science , comp-110 | comp-120 | comp-210 , ChairTemp01!
                ada.lovelace@email.franklin.edu,Ada Lovelace,Computer Science,COMP-220|COMP-230,ChairTemp02!
                """);

        List<ChairSeedRow> rows = reader.read(file);

        assertEquals(2, rows.size());
        assertEquals(
                new ChairSeedRow(
                        "grace.hopper@email.franklin.edu",
                        "Grace Hopper",
                        "Computer Science",
                        new LinkedHashSet<>(Set.of("COMP-110", "COMP-120", "COMP-210")),
                        "ChairTemp01!"),
                rows.get(0));
        assertEquals(
                new ChairSeedRow(
                        "ada.lovelace@email.franklin.edu",
                        "Ada Lovelace",
                        "Computer Science",
                        new LinkedHashSet<>(Set.of("COMP-220", "COMP-230")),
                        "ChairTemp02!"),
                rows.get(1));
    }

    /**
     * Verifies that course codes are split, trimmed, uppercased, and deduplicated.
     * @throws IOException
     */
    @Test
    void testCourseCodesAreSplitTrimmedUppercasedAndDeduplicated() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science, comp-110 | COMP-110 | comp-120 |   ,ChairTemp01!
                """);

        List<ChairSeedRow> rows = reader.read(file);

        assertEquals(1, rows.size());
        assertEquals(
                new LinkedHashSet<>(List.of("COMP-110", "COMP-120")),
                rows.get(0).courseCodes());
    }

    /**
     * Verifies that blank email fails validation.
     * @throws IOException
     */
    @Test
    void testBlankEmailFails() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                  ,Grace Hopper,Computer Science,COMP-110|COMP-120,ChairTemp01!
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("email", error.fieldOrRule());
        assertEquals("Email is required.", error.message());
    }

    /**
     * Verifies that invalid email fails validation.
     * @throws IOException
     */
    @Test
    void testInvalidEmailFails() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                not-an-email,Grace Hopper,Computer Science,COMP-110|COMP-120,ChairTemp01!
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("email", error.fieldOrRule());
        assertEquals("Email must be a valid email address.", error.message());
    }

    /**
     * Verifies that blank full name fails validation.
     * @throws IOException
     */
    @Test
    void testBlankFullNameFails() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,   ,Computer Science,COMP-110|COMP-120,ChairTemp01!
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("full_name", error.fieldOrRule());
        assertEquals("Full name is required.", error.message());
    }

    /**
     * Verifies that blank program fails validation.
     * @throws IOException
     */
    @Test
    void testBlankProgramFails() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,   ,COMP-110|COMP-120,ChairTemp01!
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("program", error.fieldOrRule());
        assertEquals("Program is required.", error.message());
    }

    /**
     * Verifies that empty normalized course codes fails validation.
     * @throws IOException
     */
    @Test
    void testEmptyNormalizedCourseCodesFail() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science, |   |  ,ChairTemp01!
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("course_codes", error.fieldOrRule());
        assertEquals("At least one course code is required.", error.message());
    }

    /**
     * Verifies that blank temp password fails validation.
     * @throws IOException
     */
    @Test
    void testBlankTempPasswordFails() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-110|COMP-120,   
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("temp_password", error.fieldOrRule());
        assertEquals("Temporary password is required.", error.message());
    }

    /**
     * Verifies that duplicate normalized emails fail validation.
     * @throws IOException
     */
    @Test
    void testDuplicateNormalizedEmailFails() throws IOException {
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                 Grace.Hopper@Email.Franklin.edu ,Grace Hopper,Computer Science,COMP-110|COMP-120,ChairTemp01!
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-210|COMP-220,ChairTemp02!
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("chairs.csv", error.fileName());
        assertEquals(3L, error.row());
        assertEquals("email", error.fieldOrRule());
        assertEquals("Duplicate email found after normalization.", error.message());
    }

    /**
     * Verifies that password values do not appear in errors.
     * @throws IOException
     */
    @Test
    void testPasswordValueDoesNotAppearInErrors() throws IOException {
        String secretPassword = "VerySecretChairPassword01!";
        Path file = writeCSV("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                  ,Grace Hopper,Computer Science,COMP-110|COMP-120,%s
                """.formatted(secretPassword));

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        assertFalse(ex.getMessage().contains(secretPassword));
        assertFalse(ex.getErrors().get(0).message().contains(secretPassword));
    }
}