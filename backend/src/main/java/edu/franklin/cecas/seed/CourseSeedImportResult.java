package edu.franklin.cecas.seed;

public record CourseSeedImportResult(int inserted, int unchanged,
    int reactivated, int deactivated) {}
