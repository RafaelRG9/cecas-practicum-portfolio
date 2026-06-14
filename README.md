# Summer Team 5

This is the team repository for Summer Team 5.

## Project

Project details follow. 

### Project Name
 Canvas Extra Credit Automation System (CECAS)

### Project Description  
The CECAS is a web application that allows students to apply for extra credit based on specific academic activities (such as seminars, competitions, and certifications). Program chairs can review, approve, or deny applications and provide feedback. This system replaces the manual, email-based process and provides an audit trail for each application's progress from submission to completion.

## Team

Team details follow

### 495 Students 

Derek Finnell

Nica Kelley

### 394 Students

Emmy Solokha

Burt Snyder

### 294 Students

Rafael Ramirez-Gaston

Alec Johnson

## Tech Stack
### Frontend
- React + TypeScript + React

### Backend
- Spring Boot

### Database
- MySQL + Flyway for schema migrations

### Email
- Mailpit

### Styling
- TailwindCSS

## Prerequisites

- Docker Desktop (recommended for running all services)

- Git (for version control)

> **Note:**  

> All required services (Spring Boot 4.0.6, React 19.2.6, Tailwind CSS 4.3.0, Mailpit 1.30.0, MySQL 8.4, Flyway 11.14.1) are managed by Docker Compose. 

> You do **not** need to install Java, Maven, Node.js, or MySQL locally unless you want to run services outside Docker for development.

## Set Up and Installation

Details on how to set up the project follow.

This project will use Docker Compose to run:
- MySQL for the database
- Mailpit for local email testing

### First Time Setup
1. Clone the repository using either ssh or https depending on how you use git.

```bash
git clone <https://github.com/2026-Summer-Franklin-CS-Practicum/2026_Summer_Team5_Repo.git>

# enter project directory
cd 2026_Summer_Team5_Repo

# switch to the develop branch
git checkout develop
```

2. Create local environment file from the example.

```bash
cp .env.example .env
```

3. Build the project

```bash
docker compose up --build -d
```

4. View App
Access the UI at: http://localhost:5173

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

## Git Workflow
Follow these steps to ensure your local code is synchronized with the team's progress.

Update Develop and Create Feature Branch
Always start by pulling the latest changes from the shared develop branch before starting new work.

```bash
git checkout develop
```
```bash
git pull
```
```bash
git checkout -b feature/your-ticket-name
```
## Finished Work: Commit and Push
Once your ticket is complete, stage your changes and push them to the remote repository.
```bash
git add .
```
```bash
git commit -m "ticket name"
```
```bash
git push -u origin feature/your-ticket-name
```
## Open Pull Request into develop on GitHub
Go to the GitHub repository website to open a Pull Request (PR) from your feature branch into develop for review.