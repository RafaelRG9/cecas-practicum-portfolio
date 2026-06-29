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

public class CategorySeedFileReaderTest {

    @TempDir
    Path tempDir;

    private final CategorySeedFileReader reader = new CategorySeedFileReader();

    private Path writeCSV(String fileName, String contents) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, contents);
        return file;
    }

    /**
     * Verifies that category rows are normalized and default points parsed.
     */
    @Test
    void testReadNormalizesValidCategoryRows() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Participation,Coming to class,5
                  Approved Seminar , Went to Seminar, 10
                """);

        List<CategorySeedRow> rows = reader.read(file);
        assertEquals(2, rows.size());
        assertEquals(new CategorySeedRow("Participation", "Coming to class", 5), rows.get(0));
        assertEquals(new CategorySeedRow("Approved Seminar", "Went to Seminar", 10), rows.get(1));
    }

    /**
     * Verifies that quoted descriptions containing commas are parsed correctly.
     */
    @Test
    void testQuotedDescriptionWithCommaParsesCorrectly() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,"Approved seminar, workshop, or event",5
                """);

        List<CategorySeedRow> rows = reader.read(file);

        assertEquals(1, rows.size());
        assertEquals(
                new CategorySeedRow("Seminar Attendance", "Approved seminar, workshop, or event", 5),
                rows.get(0));
    }

    /**
     * Verifies that blank category name fails validation.
     */
    @Test
    void testBlankCategoryNameFails() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                  ,Coming to class,5
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("categories.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("category_name", error.fieldOrRule());
        assertEquals("Category name is required.", error.message());
    }

    /**
     * Verifies that blank description fails validation.
     */
    @Test
    void testBlankDescriptionFails() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Participation,   ,5
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("categories.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("description", error.fieldOrRule());
        assertEquals("Description is required.", error.message());
    }

    /**
     * Verifies that blank default points fails validation.
     */
    @Test
    void testBlankDefaultPointsFails() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Participation,Coming to class,
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("categories.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("default_points", error.fieldOrRule());
        assertEquals("Default points is required.", error.message());
    }

    /**
     * Verifies that non-integer default points fails validation.
     */
    @Test
    void testNonIntegerDefaultPointsFails() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Participation,Coming to class,five
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("categories.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("default_points", error.fieldOrRule());
        assertEquals("Default points must be a valid integer.", error.message());
    }

    /**
     * Verifies that negative default points fails validation.
     */
    @Test
    void testNegativeDefaultPointsFails() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Participation,Coming to class,-1
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("categories.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("default_points", error.fieldOrRule());
        assertEquals("Default points must be greater than or equal to 0.", error.message());
    }

    /**
     * Verifies that duplicate category names fail after case-insensitive
     * normalization.
     */
    @Test
    void testDuplicateCategoryNamesIgnoringCaseFail() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,First description,5
                seminar attendance,Second description,10
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("categories.csv", error.fileName());
        assertEquals(3L, error.row());
        assertEquals("category_name", error.fieldOrRule());
        assertEquals("Duplicate category row found after normalization.", error.message());
    }

    /**
     * Verifies that description normalization trims leading and trailing whitespace
     * without collapsing internal whitespace.
     */
    @Test
    void testDescriptionPreservesInternalWhitespaceWhileTrimmingEdges() throws IOException {
        Path file = writeCSV("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,"  Approved   seminar   attendance  ",5
                """);

        List<CategorySeedRow> rows = reader.read(file);

        assertEquals(1, rows.size());
        assertEquals(
                new CategorySeedRow("Seminar Attendance", "Approved   seminar   attendance", 5),
                rows.get(0));
    }
}
