# Canvas Extra Credit Automation System (CECAS) - Practicum Portfolio

This repository serves as a private archive of my backend engineering contributions to the CECAS web application during the Summer 2026 CS Practicum.

## 🛠️ My Architecture & Feature Contributions

### 🔹 Ticket #11: Core User Security Domain Model
* Designed and implemented the traditional, native Java `User` entity layer mapping directly to the MySQL database schema.
* Created the foundational `UserRole` Enum to securely enforce platform access constraints between `STUDENT` and `CHAIR` actors.

### 🔹 Ticket #13: Extra Credit Request Relational Schema
* Developed the `ExtraCreditRequest` model mapping compound foreign-key relationships (`@ManyToOne`) linking users, courses, and categories.
* Architected read-only database timestamp controls (`insertable = false, updatable = false`) to enforce system audit trail integrity.
