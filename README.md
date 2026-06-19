# Canvas Extra Credit Automation System (CECAS) - Practicum Portfolio

This repository serves as a private archive of my backend engineering contributions to the CECAS web application during the Summer 2026 CS Practicum.

## 🛠️ My Architecture & Feature Contributions

### 🔹 Ticket #11: Core User Security Domain Model
* Designed and implemented the traditional, native Java `User` entity layer mapping directly to the MySQL database schema.
* Created the foundational `UserRole` Enum to securely enforce platform access constraints between `STUDENT` and `CHAIR` actors.

### 🔹 Ticket #13: Extra Credit Request Relational Schema
* Developed the `ExtraCreditRequest` model mapping compound foreign-key relationships (`@ManyToOne`) linking users, courses, and categories.
* Architected read-only database timestamp controls (`insertable = false, updatable = false`) to enforce system audit trail integrity.

### 🔹 Ticket #15: Course Data Persistence Layer
* **Objective:** Engineered the core data access object framework for academic tracking components using Spring Data JPA.
* **Implementation:** Built the `CourseRepository` interface, exposing a custom query operation `findByCourseCode(String courseCode)` leveraging implicit JPQL query generation.
* **Testing & Quality Assurance:** Developed containerized integration tests using JUnit 5, AssertJ, and **Testcontainers (MySQL 8.4)** to guarantee zero-drift production parity.

### 🔹 Ticket #16: Category Architecture & Validation
* **Objective:** Created the architectural database mapping layer for assessment categorization metrics.
* **Implementation:** Developed the `CategoryRepository` data layer and implemented the required `findByCategoryName(String categoryName)` abstraction.
* **Testing & Quality Assurance:** Activated core persistence test scaffolding and engineered targeted data validation suites inside isolated Dockerized test environments.
