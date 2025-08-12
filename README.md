# flexstore

> **Use-Case First** e-commerce demo in **Kotlin** showcasing clean hexagonal architecture (Domain ↔ Application ↔ Infrastructure) with Spring Boot adapters.

---

## 🚀 Why

`flexstore` is a minimal, opinionated template to promote **programming oriented around use cases**. Each user journey is implemented as a small, pure **application service** with:

- Explicit **REST request/response models (Jackson) mapped to pure use-case inputs/outputs**
- **Ports** (interfaces) that abstract infrastructure
- **Adapters** (REST, persistence, external APIs) that implement those ports

This repo is meant to be a learning vehicle and a ready-to-fork base for real projects.

---

## 🧱 Architecture (Hexagonal)

**Domain**: pure Kotlin (entities, value objects, policies, domain errors). No framework.\
**Infrastructure**: Spring-Boot adapters implementing ports (REST controllers as *input adapters*, JPA/HTTP as *output adapters*).

---

## 📦 Modules

```
app/                # not used at the moment
domain/             # Entities, value objects, domain services, ports, errors
infra/              # infrastructure code not linked to any specific framework
spring-infra/       # Spring adapters (REST controllers, JPA, HTTP clients)
```


## ⚙️ Quickstart

**Prerequisites**: Java 21, Gradle 8.x (wrapper included)

```bash
./gradlew clean build
./gradlew test
./gradlew :app:bootRun
```

Server starts on [http://localhost:8080](http://localhost:8080)

---

## ✅ Testing Strategy

- Unit tests at the use case level (mock ports)
- Integration tests for adapters (Testcontainers, WireMock)

---

## 🧪 Developer Experience

- Static analysis: ktlint, detekt
- Simple CI (GitHub Actions)

---

## 📄 License

MIT — see LICENSE

