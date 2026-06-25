package edu.franklin.cecas.seed;

public record SeedRunResult(CourseSeedImportResult courses, CategorySeedImportResult categories,
        ChairSeedImportResult chairs) {
    
    public String toLogSummary() {
        return "courses =" + courses + ", categories =" + categories + ", chairs =" + chairs;
    }
}
