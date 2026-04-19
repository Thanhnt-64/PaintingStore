# 📁 PROJECT STRUCTURE REVIEW & ANALYSIS

## ✅ CURRENT STRUCTURE (Already Created)

```
PaintingStore/
├── pom.xml                          (Maven config)
├── mvnw, mvnw.cmd, .mvn/           (Maven wrapper)
├── .git/, .gitignore               (Git)
├── design/
│   ├── db/
│   │   └── draft.plantuml          (DB schema)
│   └── backend/
│       ├── techList.txt
│       └── httpsIntergrate.plantuml
│
└── src/
    ├── main/
    │   ├── java/com/vn/ManageStore/
    │   │   ├── ManageStoreApplication.java
    │   │   ├── config/             ✅ CREATED
    │   │   ├── controller/         ✅ CREATED
    │   │   ├── service/            ✅ CREATED
    │   │   ├── repository/         ✅ CREATED
    │   │   ├── domain/             ✅ CREATED
    │   │   ├── mapper/             ✅ CREATED
    │   │   └── util/               ✅ CREATED
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/vn/ManageStore/
            └── ManageStoreApplicationTests.java
```

---

## ✅ WHAT'S GOOD

| Folder | Purpose | Status |
|--------|---------|--------|
| `config/` | Spring configuration classes | ✅ Ready |
| `controller/` | REST API endpoints | ✅ Ready |
| `service/` | Business logic layer | ✅ Ready |
| `repository/` | Data access layer (JPA) | ✅ Ready |
| `domain/` | Entity classes, JPA models | ✅ Ready |
| `mapper/` | DTO ↔ Entity mapping | ✅ Ready |
| `util/` | Helper utilities | ✅ Ready |

---

## ⚠️ FOLDERS MISSING (Cần thêm)

### **TIER 1 - ESSENTIAL**

#### 1. **`dto/`** - Data Transfer Objects
```
Purpose: Request/Response models (separate from JPA entities)
Location: src/main/java/com/vn/ManageStore/dto/
Example files:
  - CreateArtworkRequest.java
  - ArtworkResponse.java
  - ArtistDTO.java
  - CategoryDTO.java
  - PaginatedResponse.java
```

#### 2. **`exception/`** - Exception Handling
```
Purpose: Custom exceptions + global exception handler
Location: src/main/java/com/vn/ManageStore/exception/
Example files:
  - ResourceNotFoundException.java
  - ValidationException.java
  - GlobalExceptionHandler.java (@RestControllerAdvice)
  - ErrorResponse.java (standard error format)
```

#### 3. **`security/`** - Security Configuration
```
Purpose: JWT, authentication, authorization
Location: src/main/java/com/vn/ManageStore/security/
Example files:
  - JwtTokenProvider.java
  - JwtAuthenticationFilter.java
  - SecurityConfig.java
  - CustomUserDetailsService.java
```

#### 4. **`constant/`** - Constants & Enums
```
Purpose: System-wide constants and enumerations
Location: src/main/java/com/vn/ManageStore/constant/
Example files:
  - AppConstants.java (API paths, messages)
  - ErrorCode.java (error codes enum)
  - ArtworkType.java (enum)
  - UserRole.java (enum)
```

#### 5. **`db/migration/`** - Flyway Migrations
```
Purpose: Database schema versioning
Location: src/main/resources/db/migration/
Example files:
  - V1__init_schema.sql
  - V2__add_audit_logs.sql
  - V3__add_indexes.sql
```

#### 6. **`test/`** - Unit & Integration Tests
```
Purpose: Test code (JUnit 5, Mockito)
Location: src/test/java/com/vn/ManageStore/
Structure:
  ├── controller/
  │   └── ArtworkControllerTest.java
  ├── service/
  │   └── ArtworkServiceTest.java
  ├── repository/
  │   └── ArtworkRepositoryTest.java
  └── util/
      └── TestDataBuilder.java
```

---

### **TIER 2 - RECOMMENDED**

#### 1. **`listener/`** - Event Listeners
```
Purpose: Audit logs, event handling
Location: src/main/java/com/vn/ManageStore/listener/
Example files:
  - EntityAuditListener.java (track entity changes)
```

#### 2. **`cache/`** - Caching Logic
```
Purpose: Cache management, cache keys
Location: src/main/java/com/vn/ManageStore/cache/
Example files:
  - CacheManager.java
  - CacheKeyGenerator.java
```

#### 3. **`filter/`** - Custom Filters
```
Purpose: Request/response filters
Location: src/main/java/com/vn/ManageStore/filter/
Example files:
  - LoggingFilter.java
  - CorsFilter.java
```

#### 4. **`aop/`** - Aspect-Oriented Programming
```
Purpose: Cross-cutting concerns (logging, performance monitoring)
Location: src/main/java/com/vn/ManageStore/aop/
Example files:
  - LoggingAspect.java
  - PerformanceMonitoringAspect.java
```

---

### **TIER 3 - OPTIONAL**

#### 1. **`email/`** - Email Service
```
For: Sending notifications, event notifications
Location: src/main/java/com/vn/ManageStore/email/
```

#### 2. **`storage/`** - File Storage Service
```
For: Image upload, S3 integration
Location: src/main/java/com/vn/ManageStore/storage/
```

#### 3. **`validation/`** - Custom Validators
```
For: Complex validation logic
Location: src/main/java/com/vn/ManageStore/validation/
```

---

## 📊 RECOMMENDED FULL STRUCTURE

```
src/main/
├── java/com/vn/ManageStore/
│   ├── ManageStoreApplication.java          (Entry point)
│   ├── config/                              ✅ Already created
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── CacheConfig.java
│   │
│   ├── controller/                          ✅ Already created
│   │   ├── ArtworkController.java
│   │   ├── ArtistController.java
│   │   ├── CategoryController.java
│   │   ├── AdminAuthController.java
│   │   └── PublicGalleryController.java
│   │
│   ├── service/                             ✅ Already created
│   │   ├── ArtworkService.java
│   │   ├── ArtistService.java
│   │   ├── CategoryService.java
│   │   ├── AuthService.java
│   │   └── AuditLogService.java
│   │
│   ├── repository/                          ✅ Already created
│   │   ├── ArtworkRepository.java
│   │   ├── ArtistRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── AdminRepository.java
│   │   └── AuditLogRepository.java
│   │
│   ├── domain/                              ✅ Already created
│   │   ├── Artwork.java
│   │   ├── Artist.java
│   │   ├── Category.java
│   │   ├── Admin.java
│   │   ├── Role.java
│   │   ├── AuditLog.java
│   │   ├── Event.java
│   │   └── BaseEntity.java
│   │
│   ├── dto/                                 ⚠️ MISSING
│   │   ├── request/
│   │   │   ├── CreateArtworkRequest.java
│   │   │   ├── UpdateArtworkRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── CreateArtistRequest.java
│   │   ├── response/
│   │   │   ├── ArtworkResponse.java
│   │   │   ├── ArtistResponse.java
│   │   │   ├── AuthResponse.java
│   │   │   └── PaginatedResponse.java
│   │   └── PageDTO.java
│   │
│   ├── mapper/                              ✅ Already created
│   │   ├── ArtworkMapper.java
│   │   ├── ArtistMapper.java
│   │   └── CategoryMapper.java
│   │
│   ├── exception/                           ⚠️ MISSING
│   │   ├── ResourceNotFoundException.java
│   │   ├── ValidationException.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   └── ErrorCode.java
│   │
│   ├── security/                            ⚠️ MISSING
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── AuthUser.java
│   │   └── JwtAuthenticationEntryPoint.java
│   │
│   ├── constant/                            ⚠️ MISSING
│   │   ├── AppConstants.java
│   │   ├── ErrorCode.java
│   │   ├── ArtworkType.java
│   │   ├── UserRole.java
│   │   └── ApiEndpoints.java
│   │
│   ├── util/                                ✅ Already created
│   │   ├── DateTimeUtil.java
│   │   ├── SlugUtil.java
│   │   ├── ValidationUtil.java
│   │   └── PageableUtil.java
│   │
│   ├── listener/                            🟢 Optional
│   │   └── EntityAuditListener.java
│   │
│   ├── cache/                               🟢 Optional
│   │   ├── CacheManager.java
│   │   └── CacheKeyGenerator.java
│   │
│   ├── filter/                              🟢 Optional
│   │   ├── LoggingFilter.java
│   │   └── CorsFilter.java
│   │
│   └── aop/                                 🟢 Optional
│       ├── LoggingAspect.java
│       └── PerformanceMonitoringAspect.java
│
├── resources/
│   ├── application.properties
│   ├── application-dev.properties
│   ├── application-prod.properties
│   ├── db/migration/                        ⚠️ MISSING
│   │   ├── V1__init_schema.sql
│   │   ├── V2__add_indexes.sql
│   │   └── V3__add_audit_logs.sql
│   └── i18n/                                🟢 Optional
│       ├── messages.properties
│       └── messages_vi.properties

src/test/
├── java/com/vn/ManageStore/                 ⚠️ MINIMAL
│   ├── controller/
│   │   ├── ArtworkControllerTest.java
│   │   └── AdminAuthControllerTest.java
│   ├── service/
│   │   ├── ArtworkServiceTest.java
│   │   └── AuthServiceTest.java
│   ├── repository/
│   │   └── ArtworkRepositoryTest.java
│   ├── security/
│   │   └── JwtTokenProviderTest.java
│   └── util/
│       ├── TestDataBuilder.java
│       └── SlugUtilTest.java
│
└── resources/
    ├── application-test.properties
    └── test-data.sql
```

---

## 🎯 IMMEDIATE ACTION ITEMS

### **Priority 1: Create Essential Folders**

```bash
# From project root
mkdir -p src/main/java/com/vn/ManageStore/dto/request
mkdir -p src/main/java/com/vn/ManageStore/dto/response
mkdir -p src/main/java/com/vn/ManageStore/exception
mkdir -p src/main/java/com/vn/ManageStore/security
mkdir -p src/main/java/com/vn/ManageStore/constant
mkdir -p src/main/resources/db/migration
mkdir -p src/test/java/com/vn/ManageStore/{controller,service,repository,security,util}
mkdir -p src/test/resources
```

### **Priority 2: Create Core Files (Empty Templates)**

**DTO Layer:**
- `dto/request/CreateArtworkRequest.java`
- `dto/response/ArtworkResponse.java`
- `dto/PageDTO.java`

**Exception Layer:**
- `exception/ResourceNotFoundException.java`
- `exception/GlobalExceptionHandler.java`
- `exception/ErrorCode.java`

**Security Layer:**
- `security/JwtTokenProvider.java`
- `security/JwtAuthenticationFilter.java`
- `security/SecurityConfig.java`

**Constants:**
- `constant/AppConstants.java`
- `constant/ErrorCode.java`

**Database Migrations:**
- `resources/db/migration/V1__init_schema.sql`

**Test Base:**
- `test/util/TestDataBuilder.java`

---

## 📝 pom.xml STATUS

### ✅ Already Added:
- Spring Boot 4.0.5 ✅
- Spring Security ✅
- Spring Data JDBC ✅
- Spring Validation ✅
- Lombok ✅
- MySQL Connector ✅
- H2 Database (for testing) ✅
- Test dependencies ✅

### ❌ Missing Dependencies (Need to Add):

```xml
<!-- Flyway for migrations -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>

<!-- Spring Data JPA (NOT JDBC) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- OpenAPI / Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- Logback (for SLF4J) -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
</dependency>

<!-- Redis (optional, for caching) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- JUnit 5 & Mockito -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- TestContainers for integration tests -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

---

## ✅ NEXT STEPS (Recommended Order)

1. **Create missing folders** (DTO, Exception, Security, Constant, Migration)
2. **Update pom.xml** with missing dependencies
3. **Create base entity classes** in `domain/`
4. **Create exception handling** (@RestControllerAdvice, ErrorResponse)
5. **Create security layer** (JWT, Authentication)
6. **Create DTOs** for request/response
7. **Write database migrations** (Flyway SQL scripts)
8. **Setup application.properties** with profiles
9. **Create test structure** with test data builder

---

## 🚀 STRUCTURE RATING: 7/10

✅ **Good:** Core layers structure is solid  
⚠️ **Needs:** DTO, Exception, Security, Constant folders  
🔴 **Missing:** Database migrations, proper test structure

**Status:** Ready to extend! 🎯
