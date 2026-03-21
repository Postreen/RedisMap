# RedisMap

Проект реализует `Map<String, String>`, где данные хранятся в Redis через `Jedis`.

### Реализация
- `RedisMap` наследуется от `AbstractMap<String, String>`;
- данные хранятся в Redis Hash;
- в качестве ключа Redis используется имя мапы (`mapKey`), передаваемое в конструктор;
- поддерживаются базовые операции `Map` и стандартные default-методы интерфейса `Map`.

### Покрытие тестами
В проекте есть интеграционные тесты, которые проверяют:
- базовые CRUD-операции;
- `putAll`, `clear`;
- default-методы `Map` (`getOrDefault`, `putIfAbsent`, `replace`, `compute`, `merge`, `forEach`, `replaceAll` и т.д.);
- что `keySet()`, `values()` и `entrySet()` работают как представления, связанные с исходной мапой.

## Требования
- Java 17;
- Docker и Docker Compose;
- свободный порт `6379`.

## Как запустить Redis
Из корня проекта выполните:

```bash
  docker compose up -d
```
## Как запустить демо

```bash
  ./gradlew run
```