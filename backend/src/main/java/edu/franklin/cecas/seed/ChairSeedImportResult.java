package edu.franklin.cecas.seed;

public record ChairSeedImportResult(int inserted, int updated, int unchanged, int reactivated, int deactivated,
        int assignmentsAdded, int assignmentsRemoved) {}
