---
# Fill in the fields below to create a basic custom agent for your repository.
# The Copilot CLI can be used for local testing: https://gh.io/customagents/cli
# To make this agent available, merge this file into the default repository branch.
# For format details, see: https://gh.io/customagents/config

name: initial flexstore IA agent
description: this IA agent will produce new features in order to make this project to grow.
---

# Copilot Agent Instructions for Flexstore

**Project overview**
- Kotlin + Spring Boot application, built with Gradle.
- Hexagonal architecture: domain logic must remain framework-agnostic.
- Adapters (HTTP, persistence, etc.) live in `spring-infra/` or similar packages.

**Build & Test**
- Build: `./gradlew clean build`
- Run tests: `./gradlew test`
- Run locally: `./gradlew :app:bootRun`

**Pull Request Guidelines**
- Use Conventional Commits for titles: `feat:`, `fix:`, `chore:`, etc.
- Ensure all tests pass before submitting a PR.
- Include clear descriptions of what was changed and why.
- Add or update tests when implementing new features or fixes.
- Update documentation or README if relevant.
- Update CHANGE.md with significant changes.

**Code Quality**
- Use `ktlint` and `detekt` if configuration files are present.
- Do not add framework dependencies in the `domain` layer.
- Keep functions pure whenever possible; side effects belong in adapters.
- The business rules must be clearly expressed and easily readable by any non-technical person.
- 
**Architecture Principles**
- The domain layer defines business rules and should have no Spring annotations.
- The application layer coordinates use cases.
- The infrastructure layer handles I/O, frameworks, and configuration.

**When generating code**
- Prefer small, testable units.
- Maintain consistent naming conventions.
- Avoid duplicating logic that already exists in the codebase.

**Checklist before merging**
- [ ] All Gradle tests pass.
- [ ] Code formatted and linted.
- [ ] Proper commit message.
- [ ] Documentation updated if necessary.
