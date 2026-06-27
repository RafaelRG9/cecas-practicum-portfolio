package edu.franklin.cecas.seed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.ChairCourseAssignment;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.exception.SeedSynchronizationException;
import edu.franklin.cecas.repository.ChairCourseAssignmentRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.service.PasswordService;

@Component
public class ChairSeedImporter {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ChairCourseAssignmentRepository assignmentRepository;
    private final PasswordService passwordService;

    public ChairSeedImporter(UserRepository userRepository, CourseRepository courseRepository,
            ChairCourseAssignmentRepository assignmentRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public ChairSeedImportResult importChairs(List<ChairSeedRow> rows) {
        // Fast lookup maps so the importer can match chairs by normalized email
        // and expand each course code into all active course records with that code.
        Map<String, User> usersByEmail = userRepository.findAll().stream()
                .collect(Collectors.toMap(
                        user -> user.getEmail().trim().toLowerCase(Locale.ROOT),
                        Function.identity()));

        Map<String, List<Course>> activeCoursesByCode = courseRepository.findAllByIsActiveTrue().stream()
                .collect(Collectors.groupingBy(Course::getCourseCode));
        
        // Tracks which chair emails are still present in the current seed file so we can
        // deactivate removed chairs after the main import pass.
        Set<String> seedEmails = new HashSet<>();

        int inserted = 0;
        int updated = 0;
        int unchanged = 0;
        int reactivated = 0;
        int deactivated = 0;
        int assignmentsAdded = 0;
        int assignmentsRemoved = 0;

        for (ChairSeedRow row : rows) {
            seedEmails.add(row.email());

            // A single seeded course code may represent multiple active course sections/terms,
            // so each chair row expands into the full set of current course records it should own.
            List<Course> expectedCourses = resolveExpectedCourses(row, activeCoursesByCode);

            User existing = usersByEmail.get(row.email());
            
            // chairs.csv is not allowed to take over an email that already belongs to a non-chair user.
            if (existing != null && existing.getRole() != UserRole.CHAIR) {
                throw new SeedSynchronizationException("Seed synchronization failed: email '" + row.email()
                        + "' already belongs to a non-Chair user.");
            }

            if (existing == null) {
                // New chairs get a seeded temporary password, but that password is only used on
                // first creation. Re-seeding can never replace credentials for an existing chair.
                User saved = userRepository.save(buildNewChair(row));
                AssignmentDelta delta = syncAssignments(saved, expectedCourses);
                inserted++;
                assignmentsAdded += delta.added();
                assignmentsRemoved += delta.removed();
                continue;
            }

            boolean wasInactive = !Boolean.TRUE.equals(existing.getIsActive());
            boolean profileChanged = !Objects.equals(existing.getFullName(), row.fullName())
                    || !Objects.equals(existing.getProgram(), row.program());

            existing.setFullName(row.fullName());
            existing.setProgram(row.program());
            existing.setIsActive(true);

            AssignmentDelta delta = syncAssignments(existing, expectedCourses);
            assignmentsAdded += delta.added();
            assignmentsRemoved += delta.removed();

            if (wasInactive) {
                userRepository.save(existing);
                reactivated++;
            } else if (profileChanged) {
                userRepository.save(existing);
                updated++;
            } else {
                unchanged++;
            }
        }

        for (User user : userRepository.findAll()) {
            if (user.getRole() == UserRole.CHAIR
                    && Boolean.TRUE.equals(user.getIsActive())
                    && !seedEmails.contains(user.getEmail().trim().toLowerCase(Locale.ROOT))) {
                // Chairs missing from the latest seed are deactivated and lose current
                // assignments, but historical request records still keep their chair reference.        
                user.setIsActive(false);
                assignmentRepository.deleteAllByChairId(user.getId());
                deactivated++;
            }
        }
        return new ChairSeedImportResult(inserted, updated, unchanged, reactivated, deactivated, assignmentsAdded,
                assignmentsRemoved);
    }

    private User buildNewChair(ChairSeedRow row) {
        User user = new User();
        user.setEmail(row.email());
        user.setFullName(row.fullName());
        user.setProgram(row.program());
        user.setRole(UserRole.CHAIR);
        user.setStudentId(null);
        // Seeded chair passwords are stored as hashes, and the user must change the
        // temporary password before continuing past first login.
        user.setPassword(passwordService.encode(row.temporaryPassword()));
        user.setMustChangePassword(true);
        user.setIsActive(true);
        user.setEmailVerified(true);
        return user;
    }

    private List<Course> resolveExpectedCourses(ChairSeedRow row, Map<String, List<Course>> activeCoursesByCode) {
        List<Course> expectedCourses = new ArrayList<>();

        for (String courseCode : row.courseCodes()) {
            // Each normalized course code maps to every active course row with that code,
            // not just one section.
            List<Course> matches = activeCoursesByCode.getOrDefault(courseCode, List.of());
            expectedCourses.addAll(matches);
        }

        return expectedCourses;
    }

    private AssignmentDelta syncAssignments(User chair, List<Course> expectedCourses) {
        List<ChairCourseAssignment> currentAssignments = assignmentRepository.findAllByChairId(chair.getId());

        // Synchronization is set based:
        // add assignments that are missing,
        // keep assignments that still belong,
        // remove assignments no longer represented by the csv.
        Set<Integer> currentCourseIds = currentAssignments.stream()
                .map(assignment -> assignment.getCourse().getCourseId()).collect(Collectors.toSet());

        Set<Integer> expectedCourseIds = expectedCourses.stream().map(Course::getCourseId).collect(Collectors.toSet());

        List<ChairCourseAssignment> toAdd = expectedCourses.stream()
                .filter(course -> !currentCourseIds.contains(course.getCourseId()))
                .map(course -> new ChairCourseAssignment(chair, course)).toList();

        List<ChairCourseAssignment> toRemove = currentAssignments.stream()
                .filter(assignment -> !expectedCourseIds.contains(assignment.getCourse().getCourseId())).toList();

        assignmentRepository.saveAll(toAdd);
        assignmentRepository.deleteAll(toRemove);

        return new AssignmentDelta(toAdd.size(), toRemove.size());
    }
}
