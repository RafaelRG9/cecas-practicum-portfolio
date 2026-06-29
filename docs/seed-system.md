# Seed System Overview
This document explains how the seed system works in CECAS.

The goal of the seed system is to keep important reference data consistent across environments without relying on manual database inserts. This is mainly meant for data that changes occasionally, should be shared, and needs to be synchronized in a predictable way.

## What the Seed System Covers
The seed system is responsible for synchronizing three kinds of reference data:
- Courses
- Categories
- Chair accounts and chair-to-course assignments

These values come from CSV files stored in the repository rather than entered directly into the database.

The seed directory is:
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

## Example CSV Headers
- `courses.csv` - `course_code,term,section`
- `categories.csv` - `category_name,description,default_points`
- `chairs.csv` - `email,full_name,program,course_codes,temp_password`

## Example CSV Rows
- `courses.csv`
```
course_code,term,section
COMP-110,26/FA,H1WW
COMP-110,27/SP,H2WW
COMP-320,26/FA,H4WW
```
- `categories.csv`
```
category_name,description,default_points
Seminar Attendance,"Approved attendance at an academic or professional seminar",5
Research Presentation,"Presentation of approved research, poster work, or conference material",15
```
- `chairs.csv`
```
email,full_name,program,course_codes,temp_password
grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-110|COMP-120|COMP-210,ChairTemp01!
alan.turing@email.franklin.edu,Alan Turing,Computer Science,COMP-210|COMP-230|COMP-310,ChairTemp03!
```

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

## Common Validation Failures

Some common seed failures are:

- missing file
- empty file
- missing header row
- duplicate header names
- wrong header order
- unexpected header columns
- missing header columns
- malformed row with the wrong number of columns
- blank required values
- invalid course code format
- invalid term format
- invalid section format
- invalid Chair email
- invalid `default_points`
- negative `default_points`
- duplicate course rows after normalization
- duplicate Chair emails after normalization
- duplicate category names after normalization
- unknown Chair course codes that do not exist in `courses.csv`

A few useful row-number conventions:
- file-level errors use row `0`
- header errors use row `1`
- the first data row is row `2`

Validation errors should be safe to show in logs and tests. They should identify the file, row, and failing field or rule, but they should not expose temporary password values.

## How Synchronization Works
The seed system is designed to synchronize data, not blindly reinsert it on every run.

## Natural Keys And Matching Rules
The seed system matches rows by natural key before deciding whether to insert, update, reactivate, or deactivate anything.

- Courses use `course_code + term + section`
- Categories use `category_name`, matched case-insensitively
- Chairs use normalized `email`

That means:
- changing a course `term` or `section` creates a different course record
- changing only category capitalization updates the same category record
- changing a Chair email is treated as a different Chair record

## How Chair Course Codes Map To Terms And Sections
`chairs.csv` stores Chair ownership by course code only, like `COMP-210`.

During import, that one code expands to every active course row with the same `course_code`.

Example:
- if `courses.csv` contains `COMP-210,26/FA,H1WW`
- and `COMP-210,26/FA,H2WW`
- and `COMP-210,27/SP,H1WW`

then a Chair row with `COMP-210` will be assigned to all three active course rows.

This is why:
- one Chair can be assigned to many course rows
- multiple Chairs can share the same course
- current Chair-course ownership is tracked separately from historical request records

## Running The Seed System

In normal Docker development, startup seeding is enabled by default through `.env`.

### Start The Application
```bash
cp .env.example .env
docker compose up --build -d
```
This starts MySQL, the backend, the frontend, and Mailpit.

By default:
- frontend runs at `http://localhost:5173`
- backend runs at `http://localhost:8080`
- startup seeding runs automatically when the backend starts

### Run Manual Re-Seeding
If you edit a seed CSV and want to apply it without wiping the database, run:
```bash
make seed
```
This runs the backend once with the seed profile and executes the manual seed runner against the current database.

Use this for normal seed updates during development.

### Development Database Reset
If you want a full local rebuild from scratch, run:
```bash
make reset-db
```
This is destructive for local data. It removes the MySQL volume, reruns Flyway migrations, and then loads the current seed files again on startup.

## How To Read Seed Summary Logs

On success, the backend logs a summary like this:

- `Startup seed completed successfully: ...`
- `Manual seed completed successfully: ...`

The summary includes one result object for courses, categories, and Chairs.

### Course Summary
`inserted, unchanged, reactivated, deactivated`

### Category Summary
`inserted, updated, unchanged, reactivated, deactivated`

### Chair Summary
`inserted, updated, unchanged, reactivated, deactivated, assignmentsAdded, assignmentsRemoved`

In plain English:
- `inserted` means brand-new rows were created
- `updated` means an existing row changed in place
- `unchanged` means the row already matched the seed data
- `reactivated` means an inactive row was turned back on
- `deactivated` means a row was no longer present in the seed files
- `assignmentsAdded` and `assignmentsRemoved` describe current Chair-course ownership changes

## Running Seed-Related Tests
From the `backend/` directory, you can run the parser and validation slice with:

```bash
./mvnw -Dtest='SharedSeedCsvReaderTest,CourseSeedFileReaderTest,CategorySeedFileReaderTest,ChairSeedFileReaderTest,SeedDataParserTest' test
```
You can run the broader seed importer and service tests with:
```bash
./mvnw -Dtest='CourseSeedImporterTest,CategorySeedImporterTest,ChairSeedImporterTest,SeedServiceTest' test
```
Note:
- the importer and service tests use MySQL Testcontainers
- Docker needs to be running for those tests



## Summary
This seed system's intent is to give the project a reliable way to manage shared reference data.

In summary:
- CSV files act as the source of truth
- values are normalized before matching
- invalid data is rejected before database transactions begin
- records are synchronized rather than recreated
- removed records are typically deactivated, not deleted
- historical data related to extra credit requests is preserved