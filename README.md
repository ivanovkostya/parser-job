# parser-job — многопоточный парсер вакансий

Итоговое ДЗ по дисциплине «Теория и практика многопоточности».
Парсит вакансии с Habr Career, сохраняет в H2, отдаёт через REST.

## Требования

- JDK 25

## Запуск

./mvnw spring-boot:run

Приложение стартует на http://localhost:8080.

## Проверка

### 1. Ручной парсинг

GET http://localhost:8080/parse?pages=3

Ответ: {"pages":3,"saved":75}. Повторный запрос вернёт saved: 0 — дубликаты не сохраняются.

### 2. Получить результаты

GET http://localhost:8080/answer?page=0&size=10

Параметры:
- page — номер страницы (с 0), по умолчанию 0
- size — размер страницы, по умолчанию 10
- city — фильтр по городу (опционально)
- sort — сортировка: postedDate (по умолчанию), name, company, city

Примеры:

GET http://localhost:8080/answer?city=Москва&sort=name
GET http://localhost:8080/answer?page=1&size=20&sort=company

Ответ содержит total, page, size и массив content с полями:
id, externalId, name, company, city, salary, requirements, postedDate.

### 3. H2 Console

http://localhost:8080/h2-console

- JDBC URL: jdbc:h2:mem:parserjob
- User: sa
- Password: (пусто)

Запрос: SELECT COUNT(*) FROM VACANCIES;

### 4. Автоматический парсинг

@Scheduled запускает парсинг каждые 60 секунд (первый — через 5 секунд после старта).
В консоли видно: [scheduler] сохранено новых вакансий: N.

## Тесты
В проекте есть Unit-тесты:
- `VacancyControllerTest` — проверка эндпоинта `/answer`.
- `VacancyRepositoryTest` — проверка сохранения и дедупликации в H2.

Запуск: `./mvnw test`

## Многопоточность в проекте

- ThreadPoolExecutor (AsyncExecutorConfig, 4 core / 8 max, именованные демон-потоки) — параллельный парсинг страниц.
- CompletableFuture — объединение результатов со всех страниц.
- parallelStream — фильтрация и сортировка в /answer.
- @Scheduled — периодический запуск парсинга.

## Источник данных

Habr Career API: https://career.habr.com/api/frontend/vacancies
Не требует авторизации.
