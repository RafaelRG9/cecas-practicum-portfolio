# Seed System Overview
This document explains how the seed system is intended to work in CECAS.

The goal of the seed system is to keep important reference data consistent across environments without relying on manual database inserts. This is mainly meant for data that changes occasionally, should be shared, and needs to be synchronized in a predictable way.

> Note: This document describes the seed design and intended behavior for the project and should be considered a work in progress until implemented.

## What the Seed System Covers
The seed system is responsible for synchronizing three kinds of reference data:
- Courses
- Categories
- Chair accounts and chair-to-course assignments

These values come from CSV files stored in the repository rather than entered directly into the database.

The planned seed directory is:
```text
seed/
├── courses.csv
├── categories.csv
└── chairs.csv
```

Each file has a different purpose:
- `courses.csv` defines the active course offerings the system needs to know about.
- `categories.csv` defines the extra credit categories students can choose from.
- `chairs.csv` defines which Chair users should exist and the course codes they currently manage.

## Why Do We Need This
This project depends on a small set of shared data that should stay consistent for the whole team. For example:
- the same course should not be created multiple times in slightly different formats
- category names should stay standardized
- Chair ownership of courses should be easy to update as the data changes

This system gives us one controlled source of truth for that data.

## High Level Flow
The seed process should work in this order:
1. Read all seed files.
2. Parse the CSV rows.
3. Normalize the values into a consistent format.
4. Validate each file.
5. Perform cross-file validation.
6. Synchronize the database in a single transaction.

This means the system should reject all bad seed data before making database changes.

## Convention For Parsing Error Line Numbers
- HEADER_ROW = 1
- FIRST_DATA_ROW = 2
- FILE_ERROR_ROW = 0

## Normalization Rules
Before the data is saved, the seed process should normalize values so matching is consistent.

### Courses
For course rows:
- `course_code` - trimmed and converted to uppercase
- `term` - trimmed and converted to uppercase
- `section` - trimmed and converted to uppercase

A course is identified by its natural key:
- `course_code + term + section`
This combination determines whether a course is new, existing, or removed.

### Chairs
For chair rows:
- `email` - trimmed and converted to lowercase
- `full_name` - trimmed
- `program` - trimmed
- `course_codes` - split on `|`
- each course code - trimmed and converted to uppercase
- blank course code - remove
- duplicate course code in same row - removed

Chair records are matched by normalized email.

### Categories
For category rows:
- `category_name` - trimmed
- `description` - trimmed
- `default_points` - valid nonnegative integer

## Validation Rules
The seed process should reject invalid input before any persistence happens.

Examples of validation include:
- missing required files
- missing or incorrect headers
- blank required values
- duplicate course rows after normalization
- duplicate Chair emails after normalization
- duplicate category names, ignoring capitalization
- invalid email format
- invalid or negative `default_points`
- Chair course codes that do not exist in `courses.csv`

Validation errors should identify the file and row that failed, but should not expose password information of any type.

## How Synchronization Works
The seed system is designed to synchronize data, not blindly reinsert it on every run.

### Courses
Courses are matched by: `course_code + term + section`
- If a matching course already exists and stays active, it stays active.
- If a matching course exists but is inactive, it is reactivated.
- If a course from the file does not exist yet, it is inserted.
- If an active course is no longer present in the seed file, it is marked inactive instead of being deleted.

This matters because historical extra credit requests may still point to older course records and all data needs preserved.

### Categories
Categories are matched by name without regard to capitalization.

If a matching category exists:
- its display name can be updated
- its description can be updated
- its `default_points` can be updated
- it remains active

If a category disappears from the seed file, it is marked inactive instead of deleted.

This matters because historical extra credit requests may still point to older category records and all data needs preserved.

### Chairs
Chairs are matched by normalized email.

If a chair does not exist yet:
- create a new Chair user
- store the normalized email
- store the name and program
- hash the temporary password
- require a password change on first use
- create the correct course assignments

If a Chair already exists:
- update name and program if needed
- keep the account active
- synchonize curent course assignments
- keep the existing password
- keep existing password change state

If a chair is removed from the seed file:
- mark the chair inactive
- remove current course assignments
- keep historical request refences

A changed email is treated as a different Chair record because email is the Chair natural key.

## Chair-to-Course Assignments
Curent Chair ownership of courses is separatate from the historical Chair reference stored on an extra credit request.

This matters because:
- current Chair/course assignments determine who currently owns or reviews a course
- `extra_credit_requests.chair_id` preserves who was associated with a specific request in the past

The planned model supports:
- one Chair assigned to many courses
- multiple Chairs assigned to the same course
- no duplicate Chair/course assignments

When Chair assignments change, the system should add missing assignments and remove obsolete ones without changing any historical request records.

## Startup and Re-Seeding
This design supports two main ways to run seeding:
- automatically at startup (when enabled by configuration)
- manually when seed files are updated later

The seed directory bath should be configurable so Docker and local environments can point to the same seed file structure cleanly.

A key part of the PRD for this project is that seed data should be repeatable and safe to run more than once. Re-running the same unchanged files should not create any duplicate records.

## Summary
This seed system's intent is to give the project a reliable way to manage shared reference data.

In summary:
- CSV files act as the source of truth
- values are normalized before matching
- invalid data is rejected before database transactions begin
- records are synchronized rather than recreated
- removed records are typically deactivated, not deleted
- historical data related to extra credit requests is preserved