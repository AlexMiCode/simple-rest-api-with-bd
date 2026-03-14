# Ktor Tasks API

REST API на Ktor для работы с категориями и задачами. Проект использует реальную файловую базу H2, Exposed ORM, пул соединений HikariCP и автоматическое создание схемы через `SchemaUtils.create()` при старте приложения.

## Что реализовано

- Ktor REST API c JSON-сериализацией
- Подключение к базе данных через HikariCP
- Реальное хранение данных в H2 file database
- Две связанные таблицы: `categories` -> `tasks` (One-to-Many)
- Repository pattern с `suspend`-операциями
- `dbQuery` wrapper для транзакций Exposed
- Seed-данные при первом запуске
- Полный CRUD для категорий и задач
- Endpoint'ы для связанных данных и фильтрации
- Обработка ошибок валидации, `not found` и database constraint violations
- `CallLogging` для логирования всех запросов
- Конфигурация базы через `application.yaml` и environment variables

## Стек

- Kotlin
- Ktor
- Exposed ORM
- HikariCP
- H2 Database
- kotlinx.serialization

## Структура проекта

- `src/main/kotlin/models` - таблицы Exposed, DTO и request-модели
- `src/main/kotlin/database` - инициализация БД и `dbQuery`
- `src/main/kotlin/repository` - репозитории и доменные исключения
- `src/main/kotlin/routes` - REST endpoints
- `src/main/kotlin/plugins` - Ktor plugins
- `src/main/resources/application.yaml` - конфигурация приложения и БД

## База данных

По умолчанию проект использует файловую H2-базу:

```yaml
database:
  driver: ${DB_DRIVER:org.h2.Driver}
  url: ${DB_URL:jdbc:h2:file:./data/tasks-db;AUTO_SERVER=TRUE}
  user: ${DB_USER:sa}
  password: ${DB_PASSWORD:}
  maximumPoolSize: ${DB_MAX_POOL_SIZE:5}
```

Это значит:

- данные сохраняются после перезапуска сервера
- таблицы создаются автоматически при первом запуске
- seed-данные добавляются только при первом запуске

## Seed-данные

При первом запуске автоматически создаются категории:

- Work
- Study
- Home

Также добавляются 9 тестовых задач, привязанных к этим категориям.

## Как запустить

### 1. Проверить JDK

Проект рассчитан на Java 21.

```powershell
java -version
```

### 2. Запустить сервер

Windows PowerShell:

```powershell
.\gradlew.bat run
```

Linux/macOS:

```bash
./gradlew run
```

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

Корневой endpoint должен возвращать:

```text
Ktor tasks API is running
```

## Переменные окружения

При необходимости настройки можно переопределить через environment variables:

- `PORT`
- `DB_DRIVER`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_MAX_POOL_SIZE`

Пример для PowerShell:

```powershell
$env:PORT = "8081"
$env:DB_URL = "jdbc:h2:file:./data/custom-db;AUTO_SERVER=TRUE"
.\gradlew.bat run
```

## API endpoints

### Categories

- `GET /api/categories` - получить все категории
- `GET /api/categories/{id}` - получить категорию по id
- `POST /api/categories` - создать категорию
- `PUT /api/categories/{id}` - обновить категорию
- `DELETE /api/categories/{id}` - удалить категорию
- `GET /api/categories/{id}/tasks` - получить все задачи категории

### Tasks

- `GET /api/tasks` - получить все задачи
- `GET /api/tasks/{id}` - получить задачу по id
- `POST /api/tasks` - создать задачу
- `PUT /api/tasks/{id}` - обновить задачу
- `DELETE /api/tasks/{id}` - удалить задачу
- `GET /api/tasks?categoryId=1` - получить задачи по категории

## Формат JSON

### Создание категории

```json
{
  "name": "Fitness"
}
```

### Создание задачи

```json
{
  "title": "Go to gym",
  "description": "Leg day workout",
  "categoryId": 1
}
```

## Как тестировать проект в Postman

### 1. Создать environment

В Postman создай environment, например `Local Ktor API`, и добавь переменную:

- `baseUrl = http://localhost:8080`

После этого во всех запросах можно использовать `{{baseUrl}}`.

### 2. Создать коллекцию

Создай коллекцию `Ktor Tasks API` и добавь в нее запросы ниже.

### 3. Базовая проверка запуска

#### Request: Root

- Method: `GET`
- URL: `{{baseUrl}}/`

Ожидаемый результат:

- Status: `200 OK`
- Body: `Ktor tasks API is running`

### 4. Проверка seed-данных

#### Request: Get All Categories

- Method: `GET`
- URL: `{{baseUrl}}/api/categories`

Ожидаемый результат:

- Status: `200 OK`
- В ответе есть минимум 3 категории: `Work`, `Study`, `Home`

#### Request: Get All Tasks

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks`

Ожидаемый результат:

- Status: `200 OK`
- В ответе есть seed-задачи

### 5. CRUD для категорий

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

Ожидаемый результат:

- Status: `201 Created`
- В ответе приходит созданная категория с `id`

#### Request: Get Category By Id

- Method: `GET`
- URL: `{{baseUrl}}/api/categories/1`

Ожидаемый результат:

- Status: `200 OK`
- Возвращается категория с `id = 1`

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

Ожидаемый результат:

- Status: `200 OK`
- Имя категории обновляется

#### Request: Delete Category

- Method: `DELETE`
- URL: `{{baseUrl}}/api/categories/1`

Ожидаемый результат:

- Status: `204 No Content`

Важно: у задач настроен `onDelete = CASCADE`, поэтому при удалении категории связанные задачи тоже удаляются.

### 6. CRUD для задач

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

Ожидаемый результат:

- Status: `201 Created`
- В ответе приходит созданная задача с `id`

#### Request: Get Task By Id

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks/1`

Ожидаемый результат:

- Status: `200 OK`
- Возвращается задача с `id = 1`

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

Ожидаемый результат:

- Status: `200 OK`
- Задача обновляется

#### Request: Delete Task

- Method: `DELETE`
- URL: `{{baseUrl}}/api/tasks/1`

Ожидаемый результат:

- Status: `204 No Content`

### 7. Проверка связанных данных

#### Request: Get Tasks By Category Path

- Method: `GET`
- URL: `{{baseUrl}}/api/categories/1/tasks`

Ожидаемый результат:

- Status: `200 OK`
- Возвращается список задач, принадлежащих категории `1`

#### Request: Get Tasks By Category Query

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks?categoryId=1`

Ожидаемый результат:

- Status: `200 OK`
- Возвращаются только задачи выбранной категории

## Рекомендуемый порядок демонстрации в Postman

### 1. Проверка запуска

- `GET {{baseUrl}}/`

### 2. Проверка seed-данных

- `GET {{baseUrl}}/api/categories`
- `GET {{baseUrl}}/api/tasks`

### 3. Демонстрация связей

- `GET {{baseUrl}}/api/categories/1/tasks`
- `GET {{baseUrl}}/api/tasks?categoryId=1`

### 4. Демонстрация CRUD

- `POST {{baseUrl}}/api/categories`
- `POST {{baseUrl}}/api/tasks`
- `PUT {{baseUrl}}/api/categories/{id}`
- `PUT {{baseUrl}}/api/tasks/{id}`
- `DELETE {{baseUrl}}/api/tasks/{id}`
- `DELETE {{baseUrl}}/api/categories/{id}`

### 5. Демонстрация обработки ошибок

- создать категорию с пустым именем
- создать задачу с несуществующим `categoryId`
- создать категорию с дублирующимся именем
- запросить несуществующий `id`

## Негативные проверки в Postman

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

Ожидаемый результат:

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

Ожидаемый результат:

- Status: `400 Bad Request` или `409 Conflict`

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

Ожидаемый результат:

- Status: `409 Conflict`

### Not Found

- Method: `GET`
- URL: `{{baseUrl}}/api/tasks/9999`

Ожидаемый результат:

- Status: `404 Not Found`

## Что проверить вручную

Чтобы убедиться, что проект соответствует заданию, проверь следующее:

- сервер стартует без ошибок
- таблицы создаются автоматически при первом запуске
- seed-данные появляются только один раз
- данные сохраняются после перезапуска сервера
- CRUD работает и для категорий, и для задач
- `GET /api/categories/{id}/tasks` возвращает связанные задачи
- `GET /api/tasks?categoryId=...` корректно фильтрует данные
- при неверном `id` возвращается `404`
- при невалидном теле запроса или пустых полях возвращается `400`
- при нарушении ограничений БД возвращается `409`

## Автотесты и сборка

Если в окружении настроен Java 21, можно запустить:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Если `JAVA_HOME` не настроен, сначала нужно указать путь к JDK.

## Итог

Проект представляет собой учебный, но полноценный REST API на Ktor с persistent database storage, связями между таблицами, асинхронным repository layer и удобной проверкой через Postman.
