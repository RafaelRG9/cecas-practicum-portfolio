# Your Team Name Here

This is the team repository for <your team name here>

## Project

Project details follow. 

### Project Name
Your Project Name goes here

### Project Description  
Describe the problem solved and/or what the project is doing

## Team

Team details follow

### 495 Students 

495 student name here

### 394 Students

394 Student(s) here

### 294 Students

294 Student(s) here

## Prerequisites

- Docker Desktop
- Git

Docker will handle the following. There is no need to install separately.
- Mailpit v1.30.0
- MySQL v8.4

## Set Up and Installation

Details on how to set up the project follow.

This project will use Docker Compose to run:
- MySQL for the database
- Mailpit for local email testing

### First Time Setup
1. Clone the repository using either ssh or https depending on how you use git.

```bash
git clone <repo-url>
cd 2026_Summer_Team5_Repo
```

2. Create local environment file from the example.

```bash
cp .env.example .env
```

3. Build the project

```bash
docker compose up --build -d
```

### Using Docker

To start services:
```bash
docker compose up -d
```

To stop services:
```bash
docker compose down
```

To reset services and delete local database data (should not be needed often):
```bash
docker compose down -v
```
