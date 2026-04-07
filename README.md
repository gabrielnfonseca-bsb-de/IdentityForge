# IdentityForge

IdentityForge is an authentication and authorization service built with **Java**, **Spring Boot**, **PostgreSQL**, **JWT**, and **Kafka**, using a **Hexagonal Architecture** (Ports and Adapters).

The project is designed to be modular, testable, and ready to evolve into a production-grade identity platform. It currently supports:

* User registration
* User login
* JWT access token generation
* Refresh token flow
* Audit logging
* Role and permission modeling
* Multi-tenant support
* Event publishing through Kafka

This README explains the architecture, the role of each layer, the database design, how to run the project, how to test it, and why the main architectural decisions were made.

---

## Table of Contents

1. [Project Goal](#project-goal)
2. [Architecture Overview](#architecture-overview)
3. [Project Structure](#project-structure)
4. [Why Hexagonal Architecture](#why-hexagonal-architecture)
5. [Layer-by-Layer Explanation](#layer-by-layer-explanation)
6. [How the Layers Connect](#how-the-layers-connect)
7. [Database Design](#database-design)
8. [Understanding Tenant ID](#understanding-tenant-id)
9. [Security Design](#security-design)
10. [Kafka Integration](#kafka-integration)
11. [Running the Project](#running-the-project)
12. [Environment Variables](#environment-variables)
13. [How to Create the Database](#how-to-create-the-database)
14. [API Endpoints](#api-endpoints)
15. [Postman Testing](#postman-testing)
16. [Unit Testing](#unit-testing)
17. [Architectural Decisions](#architectural-decisions)
18. [Future Improvements](#future-improvements)

---

## Project Goal

IdentityForge is a **Custom Identity Provider (IdP)** designed to handle authentication, authorization, and identity management in a modular and secure way.

Instead of relying on external providers (like Auth0 or Keycloak), this project implements its own identity layer, giving full control over:

* authentication flows
* token generation
* multi-tenancy
* security policies
* event-driven integrations

The goal is not just to authenticate users, but to build a **secure, extensible identity platform**.

---

## Architecture Overview

This project follows **Hexagonal Architecture**, also known as **Ports and Adapters**.

At a high level:

* **Domain** contains the core business concepts and contracts
* **Application** contains use-case orchestration and DTOs
* **Infrastructure** contains technical implementations such as JPA, JWT, Kafka, and BCrypt
* **Interfaces** expose the application to the outside world, mainly through REST and configuration

The key idea is that the **business core does not depend on frameworks**. Instead, the outer layers depend on the inner layers.

---

## Project Structure

```text
domain
 ├─ model
 │   ├─ User
 │   ├─ Role
 │   └─ Permission
 │
 ├─ port
 │   ├─ in
 │   │   ├─ RegisterUserUseCase
 │   │   ├─ LoginUserUseCase
 │   │   └─ RefreshTokenUseCase
 │   │
 │   └─ out
 │       ├─ UserRepositoryPort
 │       ├─ PasswordHasherPort
 │       ├─ TokenProviderPort
 │       ├─ RefreshTokenRepositoryPort
 │       ├─ AuditLogPort
 │       └─ EventPublisherPort

application
 ├─ dto
 │   ├─ request
 │   │   ├─ RegisterRequest
 │   │   ├─ LoginRequest
 │   │   └─ RefreshTokenRequest
 │   │
 │   └─ response
 │       ├─ RegisterResponse
 │       ├─ LoginResponse
 │       └─ TokenResponse
 │
 ├─ service
 │   └─ AuthenticationService
 │
 └─ mapper (optional - future)
     └─ AuthMapper

infrastructure
 ├─ persistence
 │   ├─ entity
 │   │   ├─ UserEntity
 │   │   ├─ RoleEntity
 │   │   ├─ PermissionEntity
 │   │   ├─ RefreshTokenEntity
 │   │   └─ AuditLogEntity
 │   │
 │   ├─ repository
 │   │   ├─ JpaUserRepository
 │   │   ├─ JpaRoleRepository
 │   │   ├─ JpaPermissionRepository
 │   │   ├─ JpaRefreshTokenRepository
 │   │   └─ JpaAuditLogRepository
 │   │
 │   ├─ mapper
 │   │   └─ UserMapper
 │   │
 │   └─ adapter
 │       ├─ PostgresUserRepository
 │       ├─ RefreshTokenRepositoryAdapter
 │       └─ AuditLogAdapter
 │
 ├─ security
 │   ├─ JwtProvider
 │   ├─ JwtAuthenticationFilter
 │   └─ BCryptPasswordHasher
 │
 ├─ messaging
 │   ├─ KafkaEventPublisher
 │   └─ event
 │       ├─ UserRegisteredEvent
 │       ├─ UserLoggedInEvent
 │       └─ TokenRefreshedEvent
 │
 ├─ oauth
 │   └─ OAuth2SuccessHandler

interfaces
 ├─ rest
 │   ├─ AuthController
 │   ├─ TestController
 │   └─ dto (optional - future)
 │       ├─ LoginHttpRequest
 │       └─ LoginHttpResponse
 │
 └─ config
     ├─ BeanConfig
     └─ SecurityConfig
```

---

## Why Hexagonal Architecture

A common problem in many backend systems is that business logic becomes tightly coupled to Spring annotations, JPA entities, controllers, and framework-specific code.

This project avoids that by using ports and adapters.

### Benefits of this approach

* business rules remain independent from infrastructure
* services are easier to unit test
* technical details can be replaced without rewriting the core
* framework code stays at the edges of the system
* the project is easier to scale and refactor

For example, if JWT is replaced in the future, only the `TokenProviderPort` implementation changes. The business logic in the domain and application layers stays the same.

---

## Layer-by-Layer Explanation

## Domain

The **domain** is the core of the system.

It contains:

* the business entities
* the main rules
* the contracts the application depends on

It does **not** know about Spring Boot, JPA, HTTP, Kafka, or PostgreSQL.

### `domain/model`

These are the main business objects:

* `User`
* `Role`
* `Permission`

The `User` model represents the authenticated identity in the system. It contains business-oriented behavior such as:

* updating last login
* checking active status
* adding roles
* checking permissions indirectly through roles

`Role` and `Permission` represent authorization concepts.

### `domain/port/in`

These are the **input ports**, which define what the application can do.

They represent use cases:

* `RegisterUserUseCase`
* `LoginUserUseCase`
* `RefreshTokenUseCase`

These interfaces define the entry points of the business logic.

### `domain/port/out`

These are the **output ports**, which define what the domain/application needs from the outside world.

Examples:

* `UserRepositoryPort`
* `PasswordHasherPort`
* `TokenProviderPort`
* `RefreshTokenRepositoryPort`
* `AuditLogPort`
* `EventPublisherPort`

These ports allow the core to say *what it needs* without knowing *how it is implemented*.

---

## Application

The **application** layer orchestrates the use cases.

It sits between the domain and the outside world.

### `application/dto`

DTOs are used to transport data into and out of the use cases.

#### Request DTOs

* `RegisterRequest`
* `LoginRequest`
* `RefreshTokenRequest`

#### Response DTOs

* `RegisterResponse`
* `LoginResponse`
* `TokenResponse`

These classes are intentionally simple. They do not contain business rules.

### `application/service`

`AuthenticationService` implements the use cases defined in the domain input ports.

This class is responsible for orchestrating operations such as:

* checking if a user already exists
* hashing passwords
* generating JWTs
* saving refresh tokens
* writing audit logs
* publishing Kafka events

This is the application layer because it coordinates the work of the domain and infrastructure through ports.

### `application/mapper`

This package is currently optional and reserved for future transformations between use-case level objects.

---

## Infrastructure

The **infrastructure** layer contains the technical implementations.

This is where frameworks and external systems live.

### `infrastructure/persistence/entity`

These are JPA entities mapped to database tables:

* `UserEntity`
* `RoleEntity`
* `PermissionEntity`
* `RefreshTokenEntity`
* `AuditLogEntity`

They are persistence models, not domain models.

This separation is intentional. Domain models represent business behavior, while entities represent how data is stored.

### `infrastructure/persistence/repository`

These are Spring Data JPA repositories used to access PostgreSQL.

Examples:

* `JpaUserRepository`
* `JpaRefreshTokenRepository`
* `JpaAuditLogRepository`

### `infrastructure/persistence/mapper`

`UserMapper` converts between:

* `UserEntity` ↔ `User`

This is important because the domain should not depend on JPA entities.

### `infrastructure/persistence/adapter`

These adapters implement the output ports defined in the domain.

Examples:

* `PostgresUserRepository` implements `UserRepositoryPort`
* `RefreshTokenRepositoryAdapter` implements `RefreshTokenRepositoryPort`
* `AuditLogAdapter` implements `AuditLogPort`

This is where the hexagonal architecture becomes concrete: the domain declares the port, and infrastructure provides the adapter.

### `infrastructure/security`

This package contains security-related implementations:

* `JwtProvider` implements `TokenProviderPort`
* `BCryptPasswordHasher` implements `PasswordHasherPort`
* `JwtAuthenticationFilter` integrates JWT authentication with Spring Security

### `infrastructure/messaging`

This package contains Kafka integration.

* `KafkaEventPublisher` implements `EventPublisherPort`
* event classes represent domain/application events:

  * `UserRegisteredEvent`
  * `UserLoggedInEvent`
  * `TokenRefreshedEvent`

This allows the system to publish events without coupling business logic directly to Kafka APIs.

### `infrastructure/oauth`

`OAuth2SuccessHandler` is reserved for OAuth2 login success handling and future social-login integration.

---

## Interfaces

The **interfaces** layer is the entry point of the application.

### `interfaces/rest`

These are REST controllers exposed to clients.

* `AuthController` exposes authentication endpoints
* `TestController` can be used for protected endpoint testing

Controllers are intentionally thin. They should:

* receive HTTP input
* call the correct use case
* return HTTP responses

They should not contain business logic.

### `interfaces/config`

This package contains framework-level wiring.

* `BeanConfig` registers application and infrastructure beans
* `SecurityConfig` configures Spring Security, JWT filter, and public/private routes

This keeps Spring-specific setup away from the domain and application layers.

---

## How the Layers Connect

The dependency direction is the most important architectural rule.

```text
Interfaces -> Application -> Domain
Infrastructure -> Domain
```

A typical request flow looks like this:

```text
HTTP Request
   -> AuthController
   -> LoginUserUseCase / RegisterUserUseCase / RefreshTokenUseCase
   -> AuthenticationService
   -> Output Ports
   -> Infrastructure Adapters
   -> PostgreSQL / JWT / Kafka
```

This means:

* controllers call use cases
* services orchestrate logic
* services depend on ports
* adapters implement ports
* infrastructure details stay outside the core

---

## Database Design

The project uses PostgreSQL with a schema designed for authentication, authorization, refresh tokens, audit logs, and multi-tenancy.

### Main Tables

* `tenants`
* `users`
* `roles`
* `permissions`
* `user_roles`
* `role_permissions`
* `refresh_tokens`
* `audit_logs`

### Purpose of Each Table

#### `tenants`

Stores companies or organizations in a multi-tenant model.

#### `users`

Stores credentials and identity-related user data.

#### `roles`

Stores authorization roles such as `ADMIN` or `USER`.

#### `permissions`

Stores fine-grained permissions such as `USER_READ`, `USER_CREATE`, etc.

#### `user_roles`

Many-to-many relationship between users and roles.

#### `role_permissions`

Many-to-many relationship between roles and permissions.

#### `refresh_tokens`

Stores refresh tokens for token renewal and revocation control.

#### `audit_logs`

Stores security and identity-related actions, such as login success and token refresh.

---

## Understanding Tenant ID

The `tenantId` is one of the most important concepts in the project.

A **tenant** represents an organization, company, or client using the platform.

The system is built with **multi-tenancy** in mind. This means the same application can serve multiple organizations while keeping their data logically separated.

### What `tenantId` means

When a user is created, the `tenantId` identifies which organization that user belongs to.

In other words:

* one user belongs to one tenant
* one tenant can have many users

### Why this matters

This design makes it possible to:

* isolate data by organization
* support SaaS scenarios
* apply business rules per customer
* scale to multiple companies without changing the core architecture

### Practical example

* Tenant A = Company Alpha
* Tenant B = Company Beta

A user registered with Tenant A must be associated with the `tenantId` of Company Alpha.

### Why registration requires `tenantId`

The database enforces this relationship through a foreign key:

* `users.tenant_id` must reference `tenants.id`

This is why registration fails if the provided `tenantId` does not exist in the `tenants` table.

### Architectural rationale

Keeping `tenantId` in the core user model is a strategic decision. It makes multi-tenancy a first-class concern in the system rather than an afterthought.

---

## Security Design

Security is a first-class concern in IdentityForge.

This project is designed as a **Custom Identity Provider**, which means it must actively defend against common web and authentication attacks.

### Built-in Protections

The current implementation already helps mitigate several attack vectors:

#### 1. Password Exposure

* Passwords are never stored in plain text
* BCrypt hashing is used
* Protects against database leaks

#### 2. Token Forgery

* JWT tokens are signed with a secret key
* Prevents token tampering

#### 3. Session Hijacking (Partial)

* Refresh tokens are stored and validated in database
* Enables revocation and session control

#### 4. Broken Authentication

* Invalid credentials are handled generically
* Avoids leaking sensitive information

#### 5. Multi-Tenant Data Isolation

* `tenantId` enforces logical separation
* Prevents cross-tenant data access

#### 6. Audit Logging

* Security events (login, refresh, failures) are logged
* Enables traceability and forensic analysis

---

## Security Roadmap (Next Steps)

The next phase of the project focuses on strengthening security with formal practices and protections.

### 1. Threat Modeling

We will introduce a **threat model** to systematically identify risks such as:

* authentication bypass
* token leakage
* privilege escalation
* multi-tenant isolation breaches

This will guide future security decisions.

### 2. Input Validation

All incoming data must be validated to prevent malformed or malicious inputs.

Planned improvements:

* stricter DTO validation
* use of annotations like `@Valid`, `@Email`, `@NotBlank`
* centralized validation handling

### 3. CSRF Protection

Even though JWT reduces some CSRF risks, protection is still required depending on usage.

Planned improvements:

* enable CSRF protection where applicable
* evaluate cookie-based vs header-based token strategies

### 4. XSS Protection

Prevent execution of malicious scripts in client interactions.

Planned improvements:

* sanitize inputs
* enforce proper response encoding
* apply secure HTTP headers (Content-Security-Policy)

### 5. Security Testing

Introduce security-focused tests such as:

* invalid input fuzzing
* token manipulation attempts
* authentication bypass scenarios
* permission escalation tests

---

## Kafka Integration

Kafka is used to publish events generated by authentication workflows.

### Example events

* `UserRegisteredEvent`
* `UserLoggedInEvent`
* `TokenRefreshedEvent`

### Why use Kafka here

Kafka enables asynchronous integration without coupling the core authentication flow to secondary concerns.

Examples of future consumers:

* email notification service
* security analytics
* audit aggregation
* activity feed
* external integrations

### Architectural decision

The application depends on `EventPublisherPort`, not directly on Kafka.

This makes event publishing replaceable. Kafka is only one implementation of the port.

---

## Running the Project

### Requirements

* Java 21
* Maven
* PostgreSQL
* Docker (optional, for Kafka and other infrastructure)

### Clone the repository

```bash
git clone <your-repository-url>
cd identityforge
```

### Install dependencies and build

```bash
mvn clean install
```

### Run the application

```bash
mvn spring-boot:run
```

---

## Environment Variables

This project keeps secrets out of the source code.

Required environment variables:

```env
DB_URL=jdbc:postgresql://localhost:5433/identityforge_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your-super-secret-key
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Why this decision was made

Credentials and secrets should not be committed to the repository.

Using environment variables keeps the project safer and easier to configure across environments.

---

## How to Create the Database

### 1. Create the database

```sql
CREATE DATABASE identityforge_db;
```

### 2. Connect to it and enable UUID support

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### 3. Run the schema script

Use the SQL script defined for the project to create:

* tenants
* users
* roles
* permissions
* user_roles
* role_permissions
* refresh_tokens
* audit_logs

### 4. Insert a tenant before registration tests

```sql
INSERT INTO tenants (id, name, plan)
VALUES (uuid_generate_v4(), 'Tenant Dev', 'FREE')
RETURNING id;
```

You will need this `tenantId` when registering users.

---

## API Endpoints

### Register

`POST /auth/register`

Request body:

```json
{
  "email": "user@example.com",
  "password": "123456",
  "tenantId": "TENANT_UUID"
}
```

### Login

`POST /auth/login`

Request body:

```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

### Refresh Token

`POST /auth/refresh`

Request body:

```json
{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

---

## Postman Testing

You can test the application using Postman in this order:

1. Create a tenant in the database
2. Call `POST /auth/register`
3. Call `POST /auth/login`
4. Copy the returned `accessToken` and `refreshToken`
5. Call `POST /auth/refresh`
6. Call a protected endpoint with `Authorization: Bearer <accessToken>`

### Suggested README section for screenshots

You mentioned you will add Postman screenshots. A good structure would be:

* Register request / response
* Login request / response
* Refresh token request / response
* Protected endpoint request with JWT

---

## Unit Testing

The project includes unit tests focused on the authentication use cases.

### Main tested class

* `AuthenticationServiceTest`

### What is tested

* register success
* register with duplicated email
* login success
* login with invalid credentials
* refresh token success
* refresh token invalid case

### Why these tests matter

The service layer contains the core orchestration logic. Testing it in isolation ensures that the main business flows work correctly without depending on the database, HTTP layer, or Kafka runtime.

### How to run tests

```bash
mvn test
```

Or run a specific test class:

```bash
mvn -Dtest=AuthenticationServiceTest test
```

---

## Architectural Decisions

### 1. Hexagonal Architecture

Chosen to keep business logic independent from technical details.

### 2. Separate Domain Model and JPA Entities

Chosen to avoid leaking persistence concerns into the business core.

### 3. Ports for Technical Dependencies

Chosen so the core can depend on contracts rather than implementations.

### 4. Multi-Tenant Model

Chosen so the system can serve multiple organizations cleanly.

### 5. JWT + Refresh Token

Chosen to support stateless authentication while still allowing session lifecycle control.

### 6. Refresh Tokens Stored in Database

Chosen to enable revocation and session tracking.

### 7. Audit Logging

Chosen to provide traceability for security-sensitive actions.

### 8. Kafka Event Publishing

Chosen to decouple authentication from asynchronous integrations.

### 9. Environment Variables for Secrets

Chosen to keep credentials and secrets out of the codebase.

### 10. Hibernate `validate` Instead of `update`

Chosen because the SQL schema is treated as the source of truth, and automatic schema mutation can cause inconsistencies.

---

## Future Improvements

* assign default role automatically during registration
* add permissions to JWT claims
* implement Kafka consumers
* support OAuth2/social login flows
* add Flyway or Liquibase for database migrations
* improve exception handling with custom error responses
* add integration tests for controllers and repositories
* add tenant-aware authorization rules
* add observability and tracing

---

## Final Notes

IdentityForge is intentionally structured as more than a basic login API. It is a foundation for an identity platform that values:

* separation of concerns
* architectural clarity
* extensibility
* testability
* production-oriented design

As the project evolves, this structure allows new capabilities to be added without turning the codebase into a tightly coupled monolith.
