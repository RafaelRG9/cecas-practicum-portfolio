package edu.franklin.cecas.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.franklin.cecas.exception.SeedValidationException;

public class CourseSeedFileReaderTest {

    @TempDir
    Path tempDir;

    private final CourseSeedFileReader reader = new CourseSeedFileReader();

    private Path writeCSV(String fileName, String contents) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, contents);
        return file;
    }

    /**
     * Verifies that course rows are trimmed and uppercased.
     */
    @Test
    void testReadNormalizesCourseRows() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                 comp-110 , 26/fa , h1ww
                COMP-220,27/sp,h2ww
                """);

        List<CourseSeedRow> rows = reader.read(file);

        assertEquals(2, rows.size());
        assertEquals(new CourseSeedRow("COMP-110", "26/FA", "H1WW"), rows.get(0));
        assertEquals(new CourseSeedRow("COMP-220", "27/SP", "H2WW"), rows.get(1));
    }

    /**
     * Verifies that a blank course code fails validation.
     */
    @Test
    void testBlankCourseCodeFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                  ,26/FA,H1WW
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("course_code", error.fieldOrRule());
        assertEquals("Course code is required.", error.message());
    }

    /**
     * Verifies that a blank term fails validation.
     */
    @Test
    void testBlankTermFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                COMP-110,    ,H1WW
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("term", error.fieldOrRule());
        assertEquals("Term is required.", error.message());
    }

    /**
     * Verifies that a blank section fails validation.
     */
    @Test
    void testBlankSectionFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                COMP-110,26/FA,
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("section", error.fieldOrRule());
        assertEquals("Section is required.", error.message());
    }

    /**
     * Verifies duplicate normalized course key fails validation.
     */
    @Test
    void testDuplicateNormalizedCourseKeyFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                 comp-110 , 26/fa , h1ww
                COMP-110,26/FA,H1WW
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(3L, error.row());
        assertEquals("course_key", error.fieldOrRule());
        assertEquals("Duplicate course row found after normalization.", error.message());
    }

    /**
     * Verifies that normalized values that match the required formats are accepted.
     */
    @Test
    void testNormalizedValuesThatMatchRequiredFormatsAreAccepted() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                 comp-394 , 26/fa , h1ww
                math-120,27/sp,001a
                itec-310,26/su,2b3c
                """);

        List<CourseSeedRow> rows = reader.read(file);

        assertEquals(3, rows.size());
        assertEquals(new CourseSeedRow("COMP-394", "26/FA", "H1WW"), rows.get(0));
        assertEquals(new CourseSeedRow("MATH-120", "27/SP", "001A"), rows.get(1));
        assertEquals(new CourseSeedRow("ITEC-310", "26/SU", "2B3C"), rows.get(2));
    }

    /**
     * Verifies that a raw course code value that normalizes to an invalid format
     * fails validation.
     */
    @Test
    void testInvalidNormalizedCourseCodeFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                comp394,26/fa,h1ww
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("course_code", error.fieldOrRule());
        assertEquals("Course code must match DEPT-123 format.", error.message());
    }

    /**
     * Verifies that a raw term value that normalizes to an invalid format fails
     * validation.
     */
    @Test
    void testInvalidNormalizedTermFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                comp-394,fa/26,h1ww
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("term", error.fieldOrRule());
        assertEquals("Term must match YY/FA, YY/SP, or YY/SU format.", error.message());
    }

    /**
     * Verifies that a raw section value that normalizes to an invalid format fails
     * validation.
     */
    @Test
    void testInvalidNormalizedSectionFails() throws IOException {
        Path file = writeCSV("courses.csv", """
                course_code,term,section
                comp-394,26/fa,h1-w
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("courses.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("section", error.fieldOrRule());
        assertEquals("Section must be exactly four uppercase alphanumeric characters.", error.message());
    }
}
