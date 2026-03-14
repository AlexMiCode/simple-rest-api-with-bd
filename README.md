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
- Обработка ошибок валидации, `not found` и constraint violations
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
- при первом старте таблицы создаются автоматически
- при первом старте добавляются seed-данные

## Seed-данные

При первом запуске автоматически создаются категории:

- Work
- Study
- Home

И 9 тестовых задач, привязанных к этим категориям.

## Как запустить

### 1. Убедиться, что установлен JDK

Проект рассчитан на Java 21.

Проверка:

```powershell
java -version
```

### 2. Запустить приложение

Windows PowerShell:

```powershell
.\gradlew.bat run
```

Linux/macOS:

```bash
./gradlew run
```

После запуска сервер будет доступен по адресу:

```text
http://localhost:8080
```

Проверка health-like endpoint:

```powershell
curl http://localhost:8080/
```

Ожидаемый ответ:

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

## Как тестировать проект

Ниже набор команд, который покрывает основной сценарий проверки.

### 1. Проверить корневой endpoint

```powershell
curl http://localhost:8080/
```

### 2. Получить все категории

```powershell
curl http://localhost:8080/api/categories
```

### 3. Получить все задачи

```powershell
curl http://localhost:8080/api/tasks
```

### 4. Получить задачи категории

```powershell
curl http://localhost:8080/api/categories/1/tasks
```

### 5. Отфильтровать задачи по categoryId

```powershell
curl "http://localhost:8080/api/tasks?categoryId=1"
```

### 6. Создать новую категорию

```powershell
curl -Method POST http://localhost:8080/api/categories `
  -ContentType "application/json" `
  -Body '{"name":"Fitness"}'
```

### 7. Создать новую задачу

```powershell
curl -Method POST http://localhost:8080/api/tasks `
  -ContentType "application/json" `
  -Body '{"title":"Morning workout","description":"30 minutes cardio","categoryId":1}'
```

### 8. Обновить категорию

```powershell
curl -Method PUT http://localhost:8080/api/categories/1 `
  -ContentType "application/json" `
  -Body '{"name":"Work Updated"}'
```

### 9. Обновить задачу

```powershell
curl -Method PUT http://localhost:8080/api/tasks/1 `
  -ContentType "application/json" `
  -Body '{"title":"Updated task","description":"Updated description","categoryId":1}'
```

### 10. Удалить задачу

```powershell
curl -Method DELETE http://localhost:8080/api/tasks/1
```

### 11. Удалить категорию

```powershell
curl -Method DELETE http://localhost:8080/api/categories/1
```

Важно: у задач настроен `onDelete = CASCADE`, поэтому при удалении категории связанные задачи тоже будут удалены.

## Что проверить вручную

Чтобы убедиться, что проект соответствует заданию, стоит проверить следующее:

- сервер стартует без ошибок
- таблицы создаются автоматически при первом запуске
- seed-данные появляются только один раз
- данные сохраняются после перезапуска сервера
- CRUD работает и для категорий, и для задач
- endpoint `GET /api/categories/{id}/tasks` возвращает связанные задачи
- endpoint `GET /api/tasks?categoryId=...` корректно фильтрует данные
- при неверном `id` возвращается `404`
- при невалидном теле запроса или пустых полях возвращается `400`
- при нарушении ограничений БД возвращается `409`

## Примеры негативных проверок

### Пустое имя категории

```powershell
curl -Method POST http://localhost:8080/api/categories `
  -ContentType "application/json" `
  -Body '{"name":""}'
```

Ожидается `400 Bad Request`.

### Несуществующая категория у задачи

```powershell
curl -Method POST http://localhost:8080/api/tasks `
  -ContentType "application/json" `
  -Body '{"title":"Broken task","description":"Test","categoryId":999}'
```

Ожидается `400 Bad Request` или `409 Conflict` в зависимости от типа ошибки.

### Дубликат категории

```powershell
curl -Method POST http://localhost:8080/api/categories `
  -ContentType "application/json" `
  -Body '{"name":"Work"}'
```

Ожидается `409 Conflict`.

## Автотесты

Если в окружении настроен Java 21, можно запустить тесты и сборку:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Если `JAVA_HOME` не настроен, сначала нужно указать путь к JDK.

## Итог

Проект представляет собой учебный, но полноценный REST API на Ktor с persistent database storage, связями между таблицами, асинхронным repository layer и удобной проверкой через HTTP-запросы.
