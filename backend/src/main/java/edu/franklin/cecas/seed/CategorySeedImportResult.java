package edu.franklin.cecas.seed;

public record CategorySeedImportResult(int inserted, int updated, int unchanged,
    int reactivated, int deactivated) {}
