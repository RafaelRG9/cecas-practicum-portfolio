package edu.franklin.cecas.seed;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import edu.franklin.cecas.exception.SeedValidationException;

abstract class AbstractSeedCsvReader<T> {

    protected abstract String fileName();

    protected abstract List<String> expectedHeaders();

    protected abstract T mapRecord(CSVRecord record, long physicalRowNumber, List<SeedValidationError> errors);

    public List<T> read(Path file) {
        List<SeedValidationError> errors = new ArrayList<>();
        List<T> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();

            if (headerLine == null) {
                throw new SeedValidationException(List.of(
                        new SeedValidationError(fileName(), 1, "header", "Header row is missing.")));
            }

            CSVParser headerParser = CSVParser.parse(headerLine, CSVFormat.DEFAULT);
            List<CSVRecord> headerRecords = headerParser.getRecords();
            List<String> actualHeaders = headerRecords.get(0).toList();

            if (actualHeaders.size() != new HashSet<>(actualHeaders).size()) {
                throw new SeedValidationException(List.of(
                        new SeedValidationError(fileName(), 1, "header",
                                "Header row contains duplicate column names.")));
            }

            if (!actualHeaders.equals(expectedHeaders())) {
                throw new SeedValidationException(List.of(
                        new SeedValidationError(fileName(), 1, "header",
                                "Header row does not match expected columns.")));
            }

            CSVFormat dataFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(expectedHeaders().toArray(String[]::new))
                    .setSkipHeaderRecord(false)
                    .get();

            try (CSVParser parser = dataFormat.parse(reader)) {
                for (CSVRecord record : parser) {
                    long physicalRow = record.getRecordNumber() + 1;

                    if (record.size() != expectedHeaders().size()) {
                        errors.add(new SeedValidationError(fileName(), physicalRow,
                                "row", "Malformed row: expected %d columns but found %d."
                                        .formatted(expectedHeaders().size(), record.size())));
                        continue;
                    }

                    T mapped = mapRecord(record, physicalRow, errors);
                    if (mapped != null) {
                        rows.add(mapped);
                    }
                }
            }

        } catch (NoSuchFileException e) {
            throw new SeedValidationException(List.of(
                    new SeedValidationError(fileName(), 0, "file", "File is missing.")));
        } catch (IOException e) {
            throw new SeedValidationException(List.of(
                    new SeedValidationError(fileName(), 0, "file", "File is missing or unreadable.")));
        }

        if (!errors.isEmpty()) {
            throw new SeedValidationException(errors);
        }

        return rows;
    }
}
