-- --------------------------
-- Courses: add is_active and composite unique natural key
-- --------------------------
ALTER TABLE courses
  ADD COLUMN `is_active` BIT NOT NULL DEFAULT 1 AFTER `section`;

ALTER TABLE courses  
  MODIFY `course_code` VARCHAR(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  MODIFY `term` VARCHAR(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  MODIFY `section` VARCHAR(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

DROP INDEX `IDX1` ON courses;

ALTER TABLE courses
ADD UNIQUE INDEX `course_code_term_section_IDX` (`course_code`, `term`, `section`) VISIBLE;

-- --------------------------
-- Categories: add is_active, make name case-insensitive and unique
-- --------------------------
ALTER TABLE categories
  ADD COLUMN `is_active` BIT NOT NULL DEFAULT 1 AFTER `default_points`;

ALTER TABLE categories
  MODIFY `category_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

DROP INDEX `IDX1` ON categories;

ALTER TABLE categories
ADD UNIQUE INDEX `category_name_IDX` (`category_name`) VISIBLE;


-- --------------------------
-- Users: modify email to be case-insensitive and unique
-- --------------------------
ALTER TABLE users
    MODIFY `email` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

-- Dropping unique index to ensure email uniqueness is enforced with case-insensitivity
DROP INDEX `email_UNIQUE` ON users;

ALTER TABLE users
ADD UNIQUE INDEX `email_IDX` (`email`) VISIBLE;

-- --------------------------
-- Chair-to-course assignment table: NEW table
-- Uses a composite primary keys for many to many
-- --------------------------
CREATE TABLE IF NOT EXISTS chair_course_assignments (
  `chair_id` INT NOT NULL,
  `course_id` INT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`chair_id`, `course_id`),
    FOREIGN KEY (`chair_id`) REFERENCES users (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`course_id`) REFERENCES courses (`course_id`) ON DELETE CASCADE
);
