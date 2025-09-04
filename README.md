# 📂 File Storage API

📌 REST API для управления пользователями, файлами и историей загрузок/скачиваний.
Проект построен с использованием MVC-архитектуры, аннотаций Hibernate и покрыт миграциями через Flyway.

## 🪄 Функциональность

👤 **User**

- Создание, редактирование, удаление

- Получение по ID и списком

📁 **File**

- Загрузка файлов (multipart)

- Скачивание содержимого

- Просмотр метаданных

- Удаление и переименование

📜 **Event**

- Фиксация действий: UPLOAD, DOWNLOAD

- Просмотр событий по пользователю или файлу

- Массовое удаление событий

## 🪜 **Технологии**

- Java 17

- Jakarta Servlets (Jetty 11)

- Hibernate / JPA (аннотации)

- MySQL

- Flyway

- Maven

- Jackson

- Swagger UI

## ▶️ **Запуск**

**mysql -uroot -p -e "CREATE DATABASE filestore;"** ->
**mvn flyway:migrate** ->
**mvn jetty:run** ->
**http://localhost:8080/swagger.html**





