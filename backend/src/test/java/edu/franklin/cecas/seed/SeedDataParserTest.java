package edu.franklin.cecas.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.franklin.cecas.config.SeedProperties;
import edu.franklin.cecas.exception.SeedValidationException;

public class SeedDataParserTest {

    @TempDir
    Path tempDir;

    private Path writeCsv(String fileName, String contents) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, contents);
        return file;
    }

    /**
     * Verifies that all files can be parsed from the single parsing orchestrator.
     * 
     * @throws IOException
     */
    @Test
    void parseAllReturnsParsedSeedDataForValidFiles() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                 comp-110 , 26/fa , h1ww
                COMP-220,27/sp,h2ww
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                 Grace.Hopper@Email.Franklin.edu , Grace Hopper , Computer Science , comp-110 | comp-220 , ChairTemp01!
                ada.lovelace@email.franklin.edu,Ada Lovelace,Computer Science,COMP-220,ChairTemp02!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,"Approved seminar, workshop, or event",5
                 Research Presentation , Presented research poster , 10
                """);

        SeedProperties seedProperties = new SeedProperties(tempDir, false);
        SeedDataParser parser = new SeedDataParser(
                seedProperties,
                new CourseSeedFileReader(),
                new CategorySeedFileReader(),
                new ChairSeedFileReader());

        ParsedSeedData result = parser.parseAll();

        assertEquals(2, result.courses().size());
        assertEquals(
                List.of(
                        new CourseSeedRow("COMP-110", "26/FA", "H1WW"),
                        new CourseSeedRow("COMP-220", "27/SP", "H2WW")),
                result.courses());

        assertEquals(2, result.chairs().size());
        assertEquals(
                new ChairSeedRow(
                        "grace.hopper@email.franklin.edu",
                        "Grace Hopper",
                        "Computer Science",
                        new LinkedHashSet<>(Set.of("COMP-110", "COMP-220")),
                        "ChairTemp01!"),
                result.chairs().get(0));
        assertEquals(
                new ChairSeedRow(
                        "ada.lovelace@email.franklin.edu",
                        "Ada Lovelace",
                        "Computer Science",
                        new LinkedHashSet<>(Set.of("COMP-220")),
                        "ChairTemp02!"),
                result.chairs().get(1));

        assertEquals(2, result.categories().size());
        assertEquals(
                List.of(
                        new CategorySeedRow("Seminar Attendance", "Approved seminar, workshop, or event", 5),
                        new CategorySeedRow("Research Presentation", "Presented research poster", 10)),
                result.categories());
    }

    /**
     * Verifies that a chair course code not in courses.csv returns an error.
     * 
     * @throws IOException
     */
    @Test
    void parseAllRejectsUnknownChairCourseCode() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                COMP-110,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-999,ChairTemp01!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Approved seminar attendance,5
                """);

        SeedProperties seedProperties = new SeedProperties(tempDir, false);
        SeedDataParser parser = new SeedDataParser(
                seedProperties,
                new CourseSeedFileReader(),
                new CategorySeedFileReader(),
                new ChairSeedFileReader());

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                parser::parseAll);

        SeedValidationError error = ex.getErrors().get(0);

        assertEquals("chairs.csv", error.fileName());
        assertEquals(2L, error.row());
        assertEquals("course_codes", error.fieldOrRule());
        assertEquals("Unknown course code 'COMP-999'.", error.message());
        assertTrue(error.message().contains("COMP-999"));
    }

    /**
     * Verifies that a normalized chair course code succeeds;
     * 
     * @throws IOException
     */
    @Test
    void testNormalizedChairCourseCodeSucceedsWithValidCourse() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                COMP-110,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science, comp-110 ,ChairTemp01!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,"Approved seminar, workshop, or event",5
                Research Presentation , Presented research poster , 10
                """);

        SeedProperties seedProperties = new SeedProperties(tempDir, false);
        SeedDataParser parser = new SeedDataParser(
                seedProperties,
                new CourseSeedFileReader(),
                new CategorySeedFileReader(),
                new ChairSeedFileReader());

        ParsedSeedData result = parser.parseAll();
        assertEquals(1, result.chairs().size());
        assertEquals(new LinkedHashSet<>(Set.of("COMP-110")), result.chairs().get(0).courseCodes());
    }

    /**
     * Verifies that errors from parsing are aggregated rather than thrown
     * immediately.
     *
     * @throws IOException
     */
    @Test
    void testParseAllAggregatedErrorsFromMultipleFiles() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                 comp-110 , 26/fa , h1ww
                COMP-110,26/FA,H1WW
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Approved seminar attendance,-1
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-110,ChairTemp01!
                """);

        SeedProperties seedProperties = new SeedProperties(tempDir, false);
        SeedDataParser parser = new SeedDataParser(
                seedProperties,
                new CourseSeedFileReader(),
                new CategorySeedFileReader(),
                new ChairSeedFileReader());

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                parser::parseAll);

        assertEquals(2, ex.getErrors().size());

        assertTrue(ex.getErrors().stream().anyMatch(error -> error.fileName().equals("courses.csv")
                && error.row() == 3L
                && error.fieldOrRule().equals("course_key")
                && error.message().equals("Duplicate course row found after normalization.")));

        assertTrue(ex.getErrors().stream().anyMatch(error -> error.fileName().equals("categories.csv")
                && error.row() == 2L
                && error.fieldOrRule().equals("default_points")
                && error.message().equals("Default points must be greater than or equal to 0.")));
    }

    /**
     * Verifies that file-level and cross-file validation errors are aggregated
     * into one SeedValidationException.
     *
     * @throws IOException
     */
    @Test
    void testParseAllAggregatesFileLevelAndCrossFileErrors() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                COMP-110,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-999,ChairTemp01!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Approved seminar attendance,-1
                """);

        SeedProperties seedProperties = new SeedProperties(tempDir, false);
        SeedDataParser parser = new SeedDataParser(
                seedProperties,
                new CourseSeedFileReader(),
                new CategorySeedFileReader(),
                new ChairSeedFileReader());

        SeedValidationException ex = assertThrows(
                SeedValidationException.class,
                parser::parseAll);

        assertEquals(2, ex.getErrors().size());

        assertTrue(ex.getErrors().stream().anyMatch(error -> error.fileName().equals("chairs.csv")
                && error.row() == 2L
                && error.fieldOrRule().equals("course_codes")
                && error.message().equals("Unknown course code 'COMP-999'.")));

        assertTrue(ex.getErrors().stream().anyMatch(error -> error.fileName().equals("categories.csv")
                && error.row() == 2L
                && error.fieldOrRule().equals("default_points")
                && error.message().equals("Default points must be greater than or equal to 0.")));
    }
}
