package edu.franklin.cecas.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.franklin.cecas.exception.SeedValidationException;

public class SharedSeedCsvReaderTest {

    @TempDir
    Path tempDir;

    private final TestReader reader = new TestReader();

    static class TestReader extends AbstractSeedCsvReader<String> {
        @Override
        protected String fileName() {
            return "test.csv";
        }

        @Override
        protected List<String> expectedHeaders() {
            return List.of("col1", "col2");
        }

        @Override
        protected String mapRecord(CSVRecord record, long physicalRowNumber,
                List<SeedValidationError> errors) {
            return physicalRowNumber + ":" + record.get(0) + ":" + record.get(1);
        }
    }

    private Path writeCSV(String fileName, String contents) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, contents);
        return file;
    }

    /**
     * Verifies that a missing file returns error with row 0.
     */
    @Test
    void testMissingFileShowsRowZeroError() {
        Path missing = tempDir.resolve("missing.csv");

        SeedValidationException ex = assertThrows(
                SeedValidationException.class, () -> reader.read(missing));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals("test.csv", error.fileName());
        assertEquals(0L, error.row());
        assertEquals("file", error.fieldOrRule());
    }

    /**
     * Verifies that an empty file returns error with row 1.
     * 
     * @throws IOException
     */
    @Test
    void testEmptyFileShowsRowOneError() throws IOException {
        Path file = writeCSV("empty.csv", "");

        SeedValidationException ex = assertThrows(
                SeedValidationException.class, () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals(1L, error.row());
        assertEquals("header", error.fieldOrRule());
    }

    /**
     * Verifies that a duplicate header returns error with row 1.
     * 
     * @throws IOException
     */
    @Test
    void testDuplicateHeaderShowsRowOneError() throws IOException {
        Path file = writeCSV("duplicate-header.csv", """
                col1,col1
                a,b
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals(1L, error.row());
        assertEquals("header", error.fieldOrRule());
        assertEquals("Header row contains duplicate column names.", error.message());
    }

    /**
     * Verifies that headers in the wrong order returns error with row 1.
     * 
     * @throws IOException
     */
    @Test
    void testWrongHeaderOrderShowsRowOneError() throws IOException {
        Path file = writeCSV("wrong-order.csv", """
                col2,col1
                a,b
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals(1L, error.row());
        assertEquals("header", error.fieldOrRule());
        assertTrue(error.message().contains("Header row"));
    }

    /**
     * Verifies that missing header returns error with row 1.
     * 
     * @throws IOException
     */
    @Test
    void testMissingHeaderShowsRowOneError() throws IOException {
        Path file = writeCSV("missing-header.csv", """
                col1
                a
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        assertEquals(1L, ex.getErrors().get(0).row());
    }

    /**
     * Verifies that extra header returns error with row 1.
     * 
     * @throws IOException
     */
    @Test
    void testExtraHeaderShowsRowOneError() throws IOException {
        Path file = writeCSV("extra-header.csv", """
                col1,col2,col3
                a,b,c
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        assertEquals(1L, ex.getErrors().get(0).row());
    }

    /**
     * Verifies that a malformed row returns error with row error occurred and count
     * with
     * expected vs actual columns.
     * 
     * @throws IOException
     */
    @Test
    void testMalformedRowShowsRowNumber() throws IOException {
        Path file = writeCSV("bad-row.csv", """
                col1,col2
                a,b
                only-one-value
                """);

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                () -> reader.read(file));

        SeedValidationError error = ex.getErrors().get(0);
        assertEquals(3L, error.row());
        assertEquals("row", error.fieldOrRule());
        assertTrue(error.message().contains("expected 2 columns but found 1"));
    }

    /**
     * Verifies that the first actual data row is row 2.
     * 
     * @throws IOException
     */
    @Test
    void testFirstDataRowIsRowTwo() throws IOException {
        Path file = writeCSV("good.csv", """
                col1,col2
                a,b
                c,d
                """);

        List<String> rows = reader.read(file);

        assertEquals(List.of("2:a:b", "3:c:d"), rows);
    }

}
