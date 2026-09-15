# Zero-Knowledge Secure Password Manager (Backend)

A production-ready, high-security backend for a Zero-Knowledge Password Vault built with **Java 21**, **Spring Boot 3**, **Spring Security**, **Spring Data JPA**, **MySQL 8.0 (Docker)**, **AES-256-GCM**, and **Argon2id**.

---

## 🔒 Zero-Knowledge Security Architecture

The system enforces a strict Zero-Knowledge cryptographic design:
1. **No Master Password Transmission**: The user's Master Password never leaves the client in plaintext.
2. **Key Derivation (Argon2id)**:
   - Client derives a 256-bit symmetric encryption key (`VaultKey`) using **Argon2id** with user-specific cryptographic salt.
   - Client also derives a zero-knowledge authentication verifier token (`masterKeyVerifier`) used to verify proof-of-knowledge without exposing the key.
3. **Symmetric Vault Encryption (AES-256-GCM)**:
   - Credentials (passwords, notes) are encrypted using authenticated **AES-256-GCM** with a 12-byte cryptographically random IV and 128-bit authentication tag.
   - The server only stores the base64-encoded encrypted blob: `[IV (12 bytes)] + [Ciphertext] + [Auth Tag (16 bytes)]`.
4. **Login Password Hashing**: Account login credentials use **BCrypt** with adaptive salt rounds.
5. **No Plaintext Passwords in Logs**: The logger and audit system strictly forbid plaintext secrets from ever being recorded.

---

## 🏗️ Project Architecture

```
com.passwordmanager
├── config/
│   ├── CorsConfig.java               # CORS configuration for client/Postman
│   ├── OpenApiConfig.java            # OpenAPI 3 / Swagger configuration with Bearer JWT
│   ├── RateLimitingFilter.java       # Bucket4j IP rate limiter (Auth brute-force guard)
│   └── SecurityConfig.java           # Spring Security filter chain & stateless session
├── controller/
│   ├── AuthController.java           # Module 1: Register, Login, Setup Master Password, Change Password
│   ├── VaultController.java          # Module 2: Create, Unlock, Lock, Status
│   ├── CredentialController.java     # Module 3: CRUD for Website, DB, API Keys, Cloud, Server, Notes
│   ├── FolderController.java         # Module 4: Folders CRUD
│   ├── TagController.java            # Module 5: Color-coded Tags CRUD
│   ├── SearchController.java         # Module 6: Multi-criteria Search
│   ├── PasswordController.java       # Module 7 & 8: Password Generator & Strength/Health Check
│   └── AuditController.java          # Module 9: Audit Logs (No plaintexts)
├── dto/
│   ├── auth/                         # Requests & Responses for Auth
│   ├── vault/                        # Requests & Responses for Vault
│   ├── credential/                   # Requests & Responses for Credentials
│   ├── folder/                       # Folder DTOs
│   ├── tag/                          # Tag DTOs
│   ├── password/                     # Generator & Strength DTOs
│   ├── audit/                        # Audit log responses
│   └── common/ApiResponse.java       # Uniform ResponseEntity wrapper
├── entity/
│   ├── User.java                     # User credentials & salt/verifier metadata
│   ├── Vault.java                    # Encrypted vault key & lock state
│   ├── Credential.java               # Encrypted credentials & metadata
│   ├── CredentialType.java           # Enum: WEBSITE, DATABASE, API_KEYS, CLOUD, SERVER, SECURE_NOTES
│   ├── Folder.java                   # Folder organization
│   ├── Tag.java                      # Custom colored tags
│   ├── AuditLog.java                 # Immutable security action logs
│   └── AuditEventType.java           # LOGIN, LOGIN_FAILED, VAULT_UNLOCKED, PASSWORD_VIEWED, etc.
├── exception/
│   ├── GlobalExceptionHandler.java   # Centralized @RestControllerAdvice
│   ├── ErrorResponse.java            # Standardized error payload with validation map
│   ├── BadRequestException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── VaultLockedException.java
├── repository/                       # Spring Data JPA Repositories
├── security/
│   ├── CustomUserDetailsService.java # User loader by username/email
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java  # Bearer token validation
│   ├── JwtTokenProvider.java         # HMAC-SHA256 JWT builder & parser
│   └── UserPrincipal.java            # UserDetails implementation
├── service/                          # Business logic & transactional service layer
└── util/
    ├── Argon2Util.java               # BouncyCastle Argon2id key derivation & verification
    └── EncryptionUtil.java           # Authenticated AES-256-GCM encryption & decryption
```

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java 21** JDK installed (`java -version`)
- **Maven 3.9+** installed (`mvn -version`)
- **Docker & Docker Compose** installed (`docker compose version`)

---

### 2. Start MySQL via Docker

In the project root directory, run:
```bash
docker compose up -d
```

Verify that the MySQL container is healthy:
```bash
docker ps
```
The database `password_manager_db` will be initialized automatically using `schema.sql`.

*Docker MySQL credentials (configured in `docker-compose.yml` and `application.properties`):*
- Host: `localhost`
- Port: `3306`
- Database: `password_manager_db`
- Username: `pm_user`
- Password: `pm_password_123`
- Root Password: `root_secure_pass`

---

### 3. Build & Run the Spring Boot Application

Build the project using Maven:
```bash
mvn clean package -DskipTests
```

Run the Spring Boot application:
```bash
mvn spring-boot:run
```
Or run the packaged JAR:
```bash
java -jar target/secure-password-manager-1.0.0-SNAPSHOT.jar
```

The application starts on `http://localhost:8080`.

---

## 📖 API Documentation & Swagger UI

Once running, access the interactive OpenAPI documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

To test secured endpoints in Swagger UI:
1. Call `POST /auth/register` or `POST /auth/login`.
2. Copy the `token` from the response.
3. Click the green **Authorize** button at the top of Swagger UI.
4. Enter the token into the value field and click **Authorize**.

---

## 📮 Testing with Postman

A complete, pre-configured collection is included at `/postman_collection.json`.

### How to Import:
1. Open Postman.
2. Click **Import** (top left).
3. Select or drag-and-drop `postman_collection.json`.
4. The collection includes collection variables (`{{base_url}}`, `{{token}}`, `{{credential_id}}`).
5. Running `POST /auth/register` or `POST /auth/login` automatically stores the JWT into `{{token}}` via Postman test scripts.

---

## 📋 Comprehensive API Reference

### 1. Authentication Module (`/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | ❌ No |
| `POST` | `/auth/login` | Login with username/email & password | ❌ No |
| `POST` | `/auth/setup-master-password` | Zero-Knowledge master key setup | ✅ Bearer JWT |
| `POST` | `/auth/change-password` | Change account password & master verifier | ✅ Bearer JWT |

### 2. Vault Module (`/vault`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/vault/create` | Create new zero-knowledge encrypted vault | ✅ Bearer JWT |
| `POST` | `/vault/unlock` | Unlock vault with master key verifier | ✅ Bearer JWT |
| `POST` | `/vault/lock` | Lock the vault immediately | ✅ Bearer JWT |
| `GET` | `/vault/status` | Check lock status & credential counts | ✅ Bearer JWT |

### 3. Credential Module (`/credentials`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/credentials` | Create encrypted credential item | ✅ Bearer JWT |
| `GET` | `/credentials` | Get all credentials for user | ✅ Bearer JWT |
| `GET` | `/credentials/{id}` | Get credential by ID (logs `PASSWORD_VIEWED`) | ✅ Bearer JWT |
| `PUT` | `/credentials/{id}` | Update encrypted credential | ✅ Bearer JWT |
| `DELETE` | `/credentials/{id}` | Permanently delete credential | ✅ Bearer JWT |

### 4. Folder Module (`/folders`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/folders` | Create a new organizational folder | ✅ Bearer JWT |
| `GET` | `/folders` | List all folders owned by user | ✅ Bearer JWT |
| `PUT` | `/folders/{id}` | Update folder details | ✅ Bearer JWT |
| `DELETE` | `/folders/{id}` | Delete folder (credentials remain intact) | ✅ Bearer JWT |

### 5. Tag Module (`/tags`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/tags` | Create a custom colored tag | ✅ Bearer JWT |
| `GET` | `/tags` | List all tags for user | ✅ Bearer JWT |
| `PUT` | `/tags/{id}` | Update tag name or hex color | ✅ Bearer JWT |
| `DELETE` | `/tags/{id}` | Remove a tag | ✅ Bearer JWT |

### 6. Search Module (`/credentials/search`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/credentials/search` | Search by `name`, `url`, `username`, `type`, `folderId`, `tag` | ✅ Bearer JWT |

### 7. Password Generator (`/password`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/password/generate` | Cryptographic generator with entropy score | ❌ No |

### 8. Audit Module (`/audit`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/audit` | Retrieve security event logs (paginated) | ✅ Bearer JWT |

---

## 🛡️ Security Features Implemented
- **Stateless JWT Authentication**: Signed using HMAC-SHA256 with 24-hour configurable expiration.
- **Argon2id Key Derivation**: High memory-cost parameters (64 MB, 3 iterations) resisting GPU and ASIC attacks.
- **AES-256-GCM**: Cryptographically secure authenticated encryption with per-record random 96-bit IVs.
- **Bucket4j Rate Limiter**: 10 requests/minute per IP on authentication endpoints.
- **Jakarta Input Validation**: Enforces length constraints, valid emails, non-blank inputs.
- **Audit Logging**: Captures `LOGIN`, `LOGIN_FAILED`, `VAULT_UNLOCKED`, `PASSWORD_VIEWED`, `CREDENTIAL_*` events with IP and User-Agent tracking without leaking secrets.
