<!--
SYNC IMPACT REPORT
==================
- Version change: Unratified Template -> 1.0.0 (Initial Ratification)
- Modified principles:
  * [PRINCIPLE_1_NAME] -> I. Layered Architecture & Modular Separation
  * [PRINCIPLE_2_NAME] -> II. RESTful API Design & Standardized Contracts
  * [PRINCIPLE_3_NAME] -> III. Test-Driven Verification & Quality Standards
  * [PRINCIPLE_4_NAME] -> IV. Relational Persistence & Migration Discipline (PostgreSQL / H2)
  * [PRINCIPLE_5_NAME] -> V. Automated Verification & Review Gates
- Added sections:
  * Technology Stack & Platform Standards
  * Review Commands & Quality Gates
- Removed sections: None
- Follow-up TODOs: None
-->

# Ticket Management System Constitution

## Core Principles

### I. Layered Architecture & Modular Separation
Backend and frontend systems MUST remain strictly decoupled into modular layers:
- **Backend (Spring Boot)**: Strict controller-service-repository hierarchy. Controllers handle request mapping, deserialization, and HTTP response assembly. Services encapsulate domain transactions and business invariants. Repositories manage persistence queries. Business logic MUST NOT leak into controllers or persistence entities.
- **Frontend (React / Next.js)**: Component hierarchy divided into presentation, stateful container, and data-access layers. UI components MUST NOT call backend endpoints directly; all network interactions MUST route through dedicated, typed API client modules.
- **Rationale**: Enforces single responsibility, simplifies unit testing in isolation, and allows independent frontend/backend scaling and evolution.

### II. RESTful API Design & Standardized Contracts
All inter-service and client-server communication MUST follow explicit REST conventions:
- Resource paths MUST use pluralized nouns (e.g., `/api/v1/tickets`, `/api/v1/tickets/{id}/comments`) with standard HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- Payloads MUST use JSON formatted in `camelCase`. Response status codes MUST accurately reflect outcomes (200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 422 Unprocessable Entity, 500 Internal Error).
- Error responses MUST adhere to the RFC 7807 Problem Details specification containing `type`, `title`, `status`, `detail`, `instance`, and optional field-level validation errors.
- Every API endpoint MUST be documented with OpenAPI 3.0 / Swagger annotations; contract drift between implementation and spec is prohibited.
- **Rationale**: Prevents contract fragmentation, enables automated SDK generation, and provides predictable client integration.

### III. Test-Driven Verification & Quality Standards
Quality MUST be enforced through a comprehensive automated test pyramid:
- Unit tests MUST cover business logic and edge cases with JUnit 5 and Mockito (backend) and Vitest / React Testing Library (frontend).
- Web slice tests (`@WebMvcTest`) MUST validate request mapping, input validation, and HTTP serialization.
- Data slice tests (`@DataJpaTest`) MUST validate repository queries and custom queries.
- Integration tests (`@SpringBootTest`) MUST verify complete cross-layer flows.
- Tests MUST be deterministic, isolated, and self-cleaning. Flaky tests are treated as build blockers.
- **Rationale**: Guarantees system correctness, prevents regressions during refactoring, and ensures production readiness.

### IV. Relational Persistence & Migration Discipline (PostgreSQL / H2)
Database access and schema evolution MUST be deterministic and strictly controlled:
- PostgreSQL is the canonical database engine for production, staging, and containerized integration environments.
- H2 in-memory mode is permitted SOLELY for local rapid unit/slice testing. Dialect-specific constructs that fail on PostgreSQL are STRICTLY FORBIDDEN in application code or tests.
- Database schema changes MUST be executed via versioned migration scripts (e.g., Flyway) stored in version control. Direct manual database mutations outside migrations are prohibited.
- All transactional boundaries MUST be explicitly declared on service methods via `@Transactional`.
- **Rationale**: Eliminates environment parity discrepancies between local testing and production deployment.

### V. Automated Verification & Review Gates
No code reaches main without passing automated verification gates:
- All source files MUST compile on Java 21 without warnings and pass TypeScript strict checks.
- Code style MUST adhere to configured formatters (Spotless / Checkstyle for Java, Prettier / ESLint for TypeScript/React).
- Pull requests MUST pass all static analysis, linting, and automated tests prior to peer review approval.
- **Rationale**: Automates standard enforcement, reduces review friction, and protects mainline branch stability.

## Technology Stack & Platform Standards

The Ticket Management codebase is standardized on the following core technologies:

- **Runtime & Language**: Java 21 (LTS) with language level set to 21; Node.js 20+ (LTS) for frontend tooling.
- **Backend Framework**: Spring Boot 3.x, Spring Data JPA, Spring Web, Spring Validation (`jakarta.validation`).
- **Build Systems**: Gradle (wrapper script `./gradlew` required for backend); npm / pnpm with committed lockfile for frontend.
- **Database**: PostgreSQL 16+ (production, staging, docker-compose); H2 (in-memory mode for fast slice tests).
- **Frontend Framework**: Next.js 14+ (App Router), React 18/19, TypeScript (strict mode enabled).
- **API Documentation**: OpenAPI 3.0 via SpringDoc OpenAPI (`/swagger-ui.html`, `/v3/api-docs`).
- **Logging & Monitoring**: Structured logging (SLF4J + Logback with JSON formatting in staging/production), correlation IDs (`X-Correlation-Id`) propagated across all request lifecycles.

## Review Commands & Quality Gates

Developers and CI workflows MUST execute the following verification commands prior to review and merge:

### Backend Quality Commands
- Check style and formatting: `./gradlew spotlessCheck`
- Apply automatic formatting: `./gradlew spotlessApply`
- Run unit and slice tests: `./gradlew test`
- Run integration tests: `./gradlew integrationTest`
- Full build and gate verification: `./gradlew clean check`

### Frontend Quality Commands
- Run linter: `npm run lint`
- Run type checking: `npm run type-check` (or `npx tsc --noEmit`)
- Run unit and component tests: `npm test`
- Build production bundle: `npm run build`

### Pre-Merge Review Gate
Every pull request MUST satisfy:
1. `./gradlew check` passes with 0 failures and 0 warnings.
2. `npm run lint && npm run type-check && npm test && npm run build` passes cleanly.
3. Database migrations run cleanly and are backwards-compatible.
4. OpenAPI specification reflects all endpoint modifications.

## Governance

This Constitution supersedes all informal patterns and documentation.

- **Authority**: All architectural decisions, pull requests, and implementations MUST comply with this Constitution. Non-compliant contributions MUST NOT be merged.
- **Amendment Procedure**: Amendments MUST be submitted as a pull request updating `.specify/memory/constitution.md`, accompanied by explicit rationale, migration plan for affected modules, and approval from project maintainers.
- **Versioning Policy**: Constitution versions follow Semantic Versioning (SemVer):
  - **MAJOR**: Incompatible principle removals or foundational architecture shifts.
  - **MINOR**: Addition of new principles, stacks, or expanded operational constraints.
  - **PATCH**: Wording improvements, command adjustments, non-semantic clarifications.
- **Compliance Audits**: Regular audits during feature planning (`/speckit-plan`) and implementation (`/speckit-implement`) MUST cross-reference active constitution gates.

**Version**: 1.0.0 | **Ratified**: 2026-09-28 | **Last Amended**: 2026-09-28
