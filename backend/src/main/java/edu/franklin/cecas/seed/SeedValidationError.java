package edu.franklin.cecas.seed;

public record SeedValidationError(String fileName, long row,
    String fieldOrRule, String message) {}
