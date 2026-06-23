package edu.franklin.cecas.seed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.repository.CourseRepository;

@Component
public class CourseSeedImporter {
    private final CourseRepository courseRepository;

    public CourseSeedImporter(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseSeedImportResult importCourses(List<CourseSeedRow> rows) {
        List<Course> courses = courseRepository.findAll();

        Map<CourseNaturalKey, Course> existingByKey = new HashMap<>();
        Set<CourseNaturalKey> currentSeedKeys = new HashSet<>();
        List<Course> toInsert = new ArrayList<>();
        List<Course> toUpdate = new ArrayList<>();

        int inserted = 0;
        int unchanged = 0;
        int reactivated = 0;
        int deactivated = 0;

        for (Course course : courses) {
            existingByKey.put(new CourseNaturalKey(course.getCourseCode(), course.getTerm(), course.getSection()),
                    course);
        }
        for (CourseSeedRow row : rows) {
            CourseNaturalKey key = new CourseNaturalKey(row.courseCode(), row.term(), row.section());
            currentSeedKeys.add(key);

            Course existingCourse = existingByKey.get(key);
            if (existingCourse == null) {
                Course course = new Course(row.courseCode(), row.term(), row.section());
                course.setActive(true);
                toInsert.add(course);
                inserted++;
            } else if (existingCourse.isActive()) {
                unchanged++;
            } else {
                existingCourse.setActive(true);
                toUpdate.add(existingCourse);
                reactivated++;
            }
        }

        for (Course course : courses) {
            CourseNaturalKey key = new CourseNaturalKey(
                    course.getCourseCode(), course.getTerm(), course.getSection());

            if (course.isActive() && !currentSeedKeys.contains(key)) {
                course.setActive(false);
                toUpdate.add(course);
                deactivated++;
            }
        }

        courseRepository.saveAll(toInsert);
        courseRepository.saveAll(toUpdate);

        return new CourseSeedImportResult(inserted, unchanged, reactivated, deactivated);
    }

    private record CourseNaturalKey(String courseCode, String term, String section) {
    }
}
