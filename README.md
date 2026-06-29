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

### 🔹 Ticket #53: JPA Relational Mapping & Data Isolation Fix
* **Objective:** Investigate and resolve a critical data isolation failure where test suites returned empty results (`expected: <1> but was: <0>`) during multi-user isolation testing.
* **Implementation:** Identified a structural bug where the service layer filtered repository queries using the auto-incrementing database primary key (`student.getId()`) rather than the explicit domain business tracking column (`student.getStudentId()`). Refactored the underlying query logic in the service layer to target the correct business key.
* **Testing & Quality Assurance:** Updated the transactional test runner with clean unique parameter isolation and non-overlapping identity hashes to guarantee robust independent data separation and zero state-leakage.

### 🔹 Ticket #51: Role-Based Access Control & Security Context Injection
* **Objective:** Secure exposed API endpoints and refactored payload processing to align with modern authorization standards.
* **Implementation:** Refactored the controller architecture to drop manual identity injection via inbound DTO parameters. Integrated `@PreAuthorize("hasRole('STUDENT')")` to restrict endpoint access exclusively to authorized student profiles and utilized `@AuthenticationPrincipal UserDetails` to securely extract verified user identities directly from the session. Added `@Valid` to enforce data binding constraints.
* **Testing & Quality Assurance:** Executed the localized controller web layer testing suites to confirm role-based blocking restrictions and verify secure payload mapping.

### 🔹 Ticket #55: Core State Machine Logic Engine & Constraint Resolution
* **Objective:** Architect and implement the central backend state machine transition engine to securely manage the comprehensive lifecycle of student extra credit requests.
* **Implementation:** Built the core business logic in `StateMachineServiceImpl.java` to enforce valid state transitions (`PENDING`, `PRE_APPROVED`, `EVIDENCE_SUBMITTED`, `APPROVED`, `REJECTED`). Integrated explicit role-based access checks ensuring only a Faculty Chair can pre-approve or reject applications, and resolved a data persistence discrepancy to correctly save contextual chair feedback and final awarded points parameters into the database entity schema.
* **Testing & Quality Assurance:** Adapted the test suite to leverage the project's custom `@MySqlServiceTest` containerized environment. Resolved an underlying Hibernate data integrity constraint failure by diagnosing a non-nullable `program` database schema restriction on the user entity and refactoring the test harness data setup to guarantee a clean, 100% passing test execution without state leakage.
