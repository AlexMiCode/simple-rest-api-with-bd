# Ktor Tasks API

A REST API built with Ktor for managing categories and tasks. The project uses a real file-based H2 database, Exposed ORM, HikariCP connection pooling, and automatic schema creation through `SchemaUtils.create()` on application startup.

## Implemented Features

- Ktor REST API with JSON serialization
- Database connection through HikariCP
- Real persistent storage in an H2 file database
- Two related tables: `categories` -> `tasks` (One-to-Many)
- Repository pattern with `suspend` operations
- `dbQuery` wrapper for Exposed transactions
- Seed data on first startup
- Full CRUD for categories and tasks
- Endpoints for related data and filtering
- Validation, not found, and database constraint error handling
- `CallLogging` for request logging
- Database configuration through `application.yaml` and environment variables

## Tech Stack

- Kotlin
- Ktor
- Exposed ORM
- HikariCP
- H2 Database
- kotlinx.serialization

## Project Structure

- `src/main/kotlin/models` - Exposed table definitions, DTOs, and request models
- `src/main/kotlin/database` - database initialization and `dbQuery`
- `src/main/kotlin/repository` - repositories and domain exceptions
- `src/main/kotlin/routes` - REST endpoints
- `src/main/kotlin/plugins` - Ktor plugins
- `src/main/resources/application.yaml` - application and database configuration

## Database

By default, the project uses a file-based H2 database:

```yaml
database:
  driver: ${DB_DRIVER:org.h2.Driver}
  url: ${DB_URL:jdbc:h2:file:./data/tasks-db;AUTO_SERVER=TRUE}
  user: ${DB_USER:sa}
  password: ${DB_PASSWORD:}
  maximumPoolSize: ${DB_MAX_POOL_SIZE:5}
```

This means:

- data is preserved after server restarts
- tables are created automatically on first startup
- seed data is inserted only on first startup

## Seed Data

On first startup, the following categories are created automatically:

- Work
- Study
- Home

The application also inserts 9 test tasks linked to those categories.

## How to Run

### 1. Check JDK

The project targets Java 21.

```powershell
java -version
```

### 2. Start the Server

Windows PowerShell:

```powershell
.\gradlew.bat run
```

Linux/macOS:

```bash
./gradlew run
```

After startup, the application is available at:

```text
http://localhost:8080
```

The root endpoint should return:

```text
Ktor tasks API is running
```

## Environment Variables

You can override the configuration with environment variables:

- `PORT`
- `DB_DRIVER`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_MAX_POOL_SIZE`

Example for PowerShell:

```powershell
$env:PORT = "8081"
$env:DB_URL = "jdbc:h2:file:./data/custom-db;AUTO_SERVER=TRUE"
.\gradlew.bat run
```

## API Endpoints

### Categories

- `GET /api/categories` - get all categories
- `GET /api/categories/{id}` - get category by id
- `POST /api/categories` - create a category
- `PUT /api/categories/{id}` - update a category
- `DELETE /api/categories/{id}` - delete a category
- `GET /api/categories/{id}/tasks` - get all tasks for a category

### Tasks

- `GET /api/tasks` - get all tasks
- `GET /api/tasks/{id}` - get task by id
- `POST /api/tasks` - create a task
- `PUT /api/tasks/{id}` - update a task
- `DELETE /api/tasks/{id}` - delete a task
- `GET /api/tasks?categoryId=1` - get tasks filtered by category

## JSON Format

### Create Category

```json
{
  "name": "Fitness"
}
```

### Create Task

```json
{
  "title": "Go to gym",
  "description": "Leg day workout",
  "categoryId": 1
}
```

## How to Test the Project in Postman

### 1. Create an Environment

In Postman, create an environment, for example `Local Ktor API`, and add this variable:

- `baseUrl = http://localhost:8080`

After that, use `{{baseUrl}}` in all requests.

### 2. Create a Collection

Create a collection named `Ktor Tasks API` and add the requests listed below.

### 3. Basic Startup Check

#### Request: Root

- Method: `GET`
- URL: `{{baseUrl}}/`

Expected result:

- Status: `200 OK`
- Body: `Ktor tasks API is running`

### 4. Verify Seed Data

#### Request: Get All Categories

- Method: `GET`
- URL: `{{baseUrl}}/api/categories`

Expected result:

- Status: `200 OK`
- The response contains at least 3 categories: `Work`, `Study`, `Home`

#### Request: Get All Tasks

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks`

Expected result:

- Status: `200 OK`
- The response contains seeded tasks

### 5. Category CRUD

#### Request: Create Category

- Method: `POST`
- URL: `{{baseUrl}}/api/categories`
- Header: `Content-Type: application/json`
- Body -> raw -> JSON:

```json
{
  "name": "Fitness"
}
```

Expected result:

- Status: `201 Created`
- The response contains the created category with an `id`

#### Request: Get Category By Id

- Method: `GET`
- URL: `{{baseUrl}}/api/categories/1`

Expected result:

- Status: `200 OK`
- Returns the category with `id = 1`

#### Request: Update Category

- Method: `PUT`
- URL: `{{baseUrl}}/api/categories/1`
- Header: `Content-Type: application/json`
- Body -> raw -> JSON:

```json
{
  "name": "Work Updated"
}
```

Expected result:

- Status: `200 OK`
- The category name is updated

#### Request: Delete Category

- Method: `DELETE`
- URL: `{{baseUrl}}/api/categories/1`

Expected result:

- Status: `204 No Content`

Important: tasks use `onDelete = CASCADE`, so deleting a category also deletes its related tasks.

### 6. Task CRUD

#### Request: Create Task

- Method: `POST`
- URL: `{{baseUrl}}/api/tasks`
- Header: `Content-Type: application/json`
- Body -> raw -> JSON:

```json
{
  "title": "Morning workout",
  "description": "30 minutes cardio",
  "categoryId": 1
}
```

Expected result:

- Status: `201 Created`
- The response contains the created task with an `id`

#### Request: Get Task By Id

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks/1`

Expected result:

- Status: `200 OK`
- Returns the task with `id = 1`

#### Request: Update Task

- Method: `PUT`
- URL: `{{baseUrl}}/api/tasks/1`
- Header: `Content-Type: application/json`
- Body -> raw -> JSON:

```json
{
  "title": "Updated task",
  "description": "Updated description",
  "categoryId": 1
}
```

Expected result:

- Status: `200 OK`
- The task is updated

#### Request: Delete Task

- Method: `DELETE`
- URL: `{{baseUrl}}/api/tasks/1`

Expected result:

- Status: `204 No Content`

### 7. Related Data Checks

#### Request: Get Tasks By Category Path

- Method: `GET`
- URL: `{{baseUrl}}/api/categories/1/tasks`

Expected result:

- Status: `200 OK`
- Returns the list of tasks that belong to category `1`

#### Request: Get Tasks By Category Query

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks?categoryId=1`

Expected result:

- Status: `200 OK`
- Returns only tasks from the selected category

## Recommended Demo Order in Postman

### 1. Startup Check

- `GET {{baseUrl}}/`

### 2. Seed Data Check

- `GET {{baseUrl}}/api/categories`
- `GET {{baseUrl}}/api/tasks`

### 3. Relationship Demo

- `GET {{baseUrl}}/api/categories/1/tasks`
- `GET {{baseUrl}}/api/tasks?categoryId=1`

### 4. CRUD Demo

- `POST {{baseUrl}}/api/categories`
- `POST {{baseUrl}}/api/tasks`
- `PUT {{baseUrl}}/api/categories/{id}`
- `PUT {{baseUrl}}/api/tasks/{id}`
- `DELETE {{baseUrl}}/api/tasks/{id}`
- `DELETE {{baseUrl}}/api/categories/{id}`

### 5. Error Handling Demo

- create a category with an empty name
- create a task with a non-existing `categoryId`
- create a category with a duplicate name
- request a non-existing `id`

## Negative Test Cases in Postman

### Empty Category Name

- Method: `POST`
- URL: `{{baseUrl}}/api/categories`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "name": ""
}
```

Expected result:

- Status: `400 Bad Request`

### Invalid Category For Task

- Method: `POST`
- URL: `{{baseUrl}}/api/tasks`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "title": "Broken task",
  "description": "Test",
  "categoryId": 999
}
```

Expected result:

- Status: `400 Bad Request` or `409 Conflict`

### Duplicate Category

- Method: `POST`
- URL: `{{baseUrl}}/api/categories`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "name": "Work"
}
```

Expected result:

- Status: `409 Conflict`

### Not Found

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks/9999`

Expected result:

- Status: `404 Not Found`

## Manual Verification Checklist

To make sure the project satisfies the assignment, verify the following:

- the server starts without errors
- tables are created automatically on first startup
- seed data appears only once
- data is preserved after server restarts
- CRUD works for both categories and tasks
- `GET /api/categories/{id}/tasks` returns related tasks
- `GET /api/tasks?categoryId=...` filters data correctly
- invalid `id` returns `404`
- invalid request body or blank fields return `400`
- database constraint violations return `409`

## Automated Tests and Build

If Java 21 is configured in the environment, you can run:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

If `JAVA_HOME` is not configured, set it before running Gradle.

## Summary

This project is a small but complete Ktor REST API with persistent database storage, table relationships, an asynchronous repository layer, and a clear Postman-based testing workflow.
