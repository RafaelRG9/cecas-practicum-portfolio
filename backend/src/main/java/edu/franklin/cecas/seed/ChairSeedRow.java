package edu.franklin.cecas.seed;

import java.util.Set;

public record ChairSeedRow(
        String email,
        String fullName,
        String program,
        Set<String> courseCodes,
        String temporaryPassword) {
}
