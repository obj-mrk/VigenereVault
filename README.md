# Vigenere Vault

Vigenere Vault —  Spring Boot-приложение для асинхронного шифрования и расшифровки текста с использованием шифра Виженера.

Приложение предоставляет REST API и простой статический frontend. Задачи шифрования сохраняются в PostgreSQL и обрабатываются асинхронно.

## Технологии

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- Docker / Docker Compose
- Vanilla HTML/CSS/JavaScript

## Возможности

- отправка текста на шифрование;
- асинхронная обработка задачи;
- получение статуса задачи;
- получение зашифрованного текста;
- расшифровка по `jobId` и ключу;
- web-интерфейс для ручной работы.

## Архитектура

Проект разделён на несколько слоёв:

```text
mrk
├── application      # use cases, commands, results, ports
├── domain           # domain model, business rules, exceptions
├── infrastructure   # persistence, async processing, crypto implementation
├── presentation     # REST API, requests, responses, exception handling
└── config           # Spring configuration
