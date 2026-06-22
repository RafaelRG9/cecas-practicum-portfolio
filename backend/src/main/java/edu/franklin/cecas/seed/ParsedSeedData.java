package edu.franklin.cecas.seed;

import java.util.List;

public record ParsedSeedData(List<CourseSeedRow> courses,
        List<ChairSeedRow> chairs, List<CategorySeedRow> categories) {}
