-- -----------------------------------------------------
-- Table `courses`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `courses` (
  `course_id` INT NOT NULL AUTO_INCREMENT,
  `course_code` VARCHAR(45) NOT NULL,
  `term` VARCHAR(45) NOT NULL,
  `section` VARCHAR(45) NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`course_id`),
  INDEX `IDX1` (`course_code` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `role` ENUM('STUDENT', 'CHAIR') NOT NULL,
  `student_id` INT NULL DEFAULT NULL,
  `program` VARCHAR(45) NOT NULL,
  `is_active` BIT NOT NULL DEFAULT 1,
  `must_change_password` BIT NOT NULL DEFAULT 0,
  `email_verified` BIT NULL DEFAULT 0,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `categories`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `category_name` VARCHAR(45) NOT NULL,
  `description` VARCHAR(1000) NOT NULL,
  `default_points` INT NOT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  INDEX `IDX1` (`category_name` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `extra_credit_requests`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `extra_credit_requests` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(1000) NOT NULL,
  `student_id` INT NOT NULL,
  `chair_id` INT NULL,
  `course_id` INT NOT NULL,
  `category_id` INT NOT NULL,
  `status` ENUM('PENDING', 'PRE_APPROVED', 'REJECTED', 'EVIDENCE_SUBMITTED', 'CLOSED', 'APPROVED') NOT NULL,
  `evidence_file_path` VARCHAR(255) NULL DEFAULT NULL,
  `due_date` DATETIME NULL DEFAULT NULL,
  `awarded_points` INT NULL,
  `chair_feedback` VARCHAR(1000) NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `FK_ecr_users_idx` (`student_id` ASC) VISIBLE,
  INDEX `FK2_ecr_users_idx` (`chair_id` ASC) VISIBLE,
  INDEX `FK_ecr_courses_idx` (`course_id` ASC) VISIBLE,
  INDEX `FK_ecr_categories_idx` (`category_id` ASC) VISIBLE,
  INDEX `IDX1` (`status` ASC) VISIBLE,
  CONSTRAINT `FK_ecr_users`
    FOREIGN KEY (`student_id`)
    REFERENCES `users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK2_ecr_users`
    FOREIGN KEY (`chair_id`)
    REFERENCES `users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_ecr_courses`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`course_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK_ecr_categories`
    FOREIGN KEY (`category_id`)
    REFERENCES `categories` (`category_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
