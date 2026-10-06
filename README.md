# ICE Services — Music Metadata Platform

**Author:** Shakil Jansberg  
**Role:** Senior Backend Engineer  
**Tech Stack:** Java 21 / Spring Boot 3.3 / Angular 18 / PostgreSQL / Docker  

---

## 1. Overview & Architecture Strategy

This repository contains the complete implementation of the **Music Metadata Platform** for managing music catalog records (artists, aliases, tracks, and discographies) and powering an automated daily promotional rotation on the platform homepage.

### Architectural Approach: Modular Monolith
Rather than introducing distributed microservice complexity (multi-repo overhead, distributed transactions, network latency, and operational fragility for a focused domain), the service is engineered as a **Modular Monolith** adhering to strict **Domain-Driven Design (DDD) Bounded Contexts**:

1. **Strict Logical Boundaries**: Packages (`artist`, `track`, `catalogue`, `artistofday`, `shared`) are decoupled. Modules interact through clean service interfaces and immutable DTO records rather than accessing other domains' repositories or database entities directly.
2. **Immutable Identity vs. Mutable Data**: Artists are identified by an immutable UUID (`id`). Modifying an artist's canonical name automatically archives the former name into an `artist_aliases` table, ensuring existing track ownership and royalty attributions remain intact without breaking history.
3. **Stateless, Pure Cyclical Rotation**: The "Artist of the Day" uses a deterministic mathematical formula ($\text{epochDay} \pmod N$) ordered stably by `(created_at ASC, id ASC)`. It guarantees fair cyclical repetition across all $N$ artists every $N$ days with zero race conditions, database locks, or reliance on midnight cron jobs.
4. **Production-Ready Standards**:
   - Multi-stage, non-root Docker containers.
   - Stateless JWT authentication with Role-Based Access Control (`ROLE_EDITOR` for mutations, public reads).
   - Distributed tracing via `X-Correlation-ID` header and SLF4J MDC logging.
   - Database schema migrations managed through Flyway.
   - High-throughput in-memory caching via Caffeine with automatic midnight UTC expiry and mutation-driven cache eviction.
   - Standardized RFC 7807 Problem Details error responses with zero internal stack trace leakage.

---

## 2. Package & Module Structure

```
com.ice.music
├── artist/             # Bounded Context: Artist Lifecycle & Alias History
│   ├── controller/     # POST /api/v1/artists, PATCH /api/v1/artists/{id}, GET /api/v1/artists/{id}
│   ├── dto/            # Immutable Records: CreateArtistRequest, UpdateArtistRequest, ArtistResponse
│   ├── model/          # JPA Entities: Artist, ArtistAlias (Optimistic Locking via @Version)
│   ├── repository/     # ArtistRepository (Deterministic sequence queries)
│   └── service/        # ArtistService (Alias archival, cache eviction, transactional boundaries)
│
├── track/              # Bounded Context: Track Ingestion & Metadata
│   ├── controller/     # POST /api/v1/tracks, GET /api/v1/tracks/{id}
│   ├── dto/            # CreateTrackRequest, TrackResponse (Formatted duration: mm:ss)
│   ├── model/          # Track Entity (Indexed artistId, genre, durationMs, ISRC)
│   ├── repository/     # TrackRepository (Optimized artist discography lookup)
│   └── service/        # TrackService (Validates artist existence, enforces invariants)
│
├── catalogue/          # Bounded Context: High-Throughput Read Model & Discography
│   ├── controller/     # GET /api/v1/catalogue/artists/{id}/tracks (Paginated, Genre filter)
│   ├── dto/            # ArtistDiscographyResponse (Artist header + available genres + tracks)
│   └── service/        # CatalogueService (Aggregates query projections without mutating state)
│
├── artistofday/        # Bounded Context: Deterministic Fair Rotation Engine
│   ├── controller/     # GET /api/v1/featured/today, GET /api/v1/featured/date/{date}
│   ├── dto/            # ArtistOfTheDayResponse (Cycle index, total artists, UTC countdown)
│   └── service/        # CyclicalRotationEngine (Pure modulo math + Caffeine L1 Cache)
│
└── shared/             # Cross-Cutting Infrastructure
    ├── config/         # OpenApiConfig (Swagger 3.0 / BearerAuth), CacheConfig
    ├── exception/      # GlobalExceptionHandler (RFC 7807), ResourceNotFoundException
    ├── filter/         # CorrelationIdFilter (MDC X-Correlation-ID tracing)
    └── security/       # SecurityConfig, JwtUtil, JwtAuthenticationFilter, AuthController
```

---

## 3. Core Functional Requirements & Implementation

### Requirement 1: Add a New Track to an Artist's Catalogue
* **Endpoint**: `POST /api/v1/tracks` (Secured: requires `ROLE_EDITOR`).
* **Validation**: Validates that `artistId` exists in the database, `durationMs > 0`, and required attributes are present.
* **Duration Formatting**: Computes a human-readable duration (`mm:ss`, e.g., `215000 ms` $\rightarrow$ `"03:35"`) on ingestion for client efficiency.

### Requirement 2: Edit Artist Name & Manage Aliases
* **Endpoint**: `PATCH /api/v1/artists/{id}` (Secured: requires `ROLE_EDITOR`).
* **Auto-Alias Preservation**: When an artist's canonical name is updated from $A$ to $B$, former name $A$ is automatically moved to the `artist_aliases` table. All associated tracks reference the immutable artist UUID, preserving 100% catalog integrity.
* **Concurrency Control**: Protected by JPA `@Version` column against concurrent lost updates (returns HTTP 409 Conflict upon version conflict).

### Requirement 3: Fetch Artist Tracks
* **Endpoint**: `GET /api/v1/catalogue/artists/{id}/tracks?genre=...&page=0&size=20` (Public).
* **Read Model**: Optimized query leveraging index `idx_tracks_artist_id`. Returns the artist header, alias history, total track count, distinct genres summary, and the ordered track discography.

### Requirement 4: Fair, Cyclical "Artist of the Day"
* **Endpoint**: `GET /api/v1/featured/today` (Public).
* **Formula**:
  $$\text{epochDay} = \text{LocalDate.now(ZoneOffset.UTC).toEpochDay()}$$
  $$\text{targetIndex} = \text{Math.floorMod}(\text{epochDay}, N)$$
  with deterministic compound database ordering:
  ```sql
  SELECT * FROM artists 
  WHERE active = true 
  ORDER BY created_at ASC, id ASC 
  LIMIT 1 OFFSET :targetIndex;
  ```
* **Fairness Guarantee**: For any sequence of $N$ consecutive calendar days, every single active artist is featured exactly once. On day $N+1$, the cycle cleanly restarts with artist 0.
* **Caching & Real-Time Countdown**: Cached via Caffeine with expiry at midnight UTC. The API returns `secondsRemainingInDay` until midnight UTC, powering a live countdown timer in the frontend.

---

## 4. How to Run

### Option A: One-Click Local Runner (Windows)
```cmd
run-local.bat
```
Starts both the Spring Boot backend (port 8080) and the Angular frontend (port 4200) in separate terminals.

---

### Option B: Docker Compose (Full Stack with PostgreSQL)
```bash
docker compose up --build
```
Launches:
* **PostgreSQL 16**: Port 5432
* **Spring Boot Backend**: Port 8080
* **Angular 18 Frontend**: Port 4200 (served via NGINX)

---

### Option C: Manual Local Startup

#### 1. Backend (Spring Boot 3.3)
```bash
cd backend
# Windows:
.\mvnw.cmd spring-boot:run

# Linux / macOS:
./mvnw spring-boot:run
```
*(Runs with the default `dev` profile using in-memory H2 database with PostgreSQL compatibility and automatic Flyway migrations).*

#### 2. Frontend (Angular 18)
```bash
cd frontend
npm install
npm start
```

---

### Application Access URLs
* **Frontend Developer Console**: [http://localhost:4200](http://localhost:4200)
* **Backend API & Actuator**: [http://localhost:8080](http://localhost:8080)
* **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Actuator Health Probe**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
* **H2 Database Console** (Dev profile): [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:ice_music_db`, User: `sa`, Password: *empty*)

---

## 5. Automated Testing & Verification

The test suite covers algorithmic proofs, domain invariants, security authorization, and REST endpoints:

```bash
cd backend
# Windows:
.\mvnw.cmd clean test

# Linux / macOS:
./mvnw clean test
```

### Verified Test Suites (11 Tests, 100% Pass Rate):
1. **`CyclicalRotationEngineTest`** (3 tests):
   - Proves mathematically that rotation cycles across $N$ artists and Day $N+1$ restarts at Day 1.
   - Validates graceful handling of empty catalog ($N=0$).
   - Validates negative Julian epoch day handling (pre-1970 dates) without modulo index errors.
2. **`ArtistServiceTest`** (3 tests):
   - Validates artist creation with initial aliases.
   - Validates that updating canonical name automatically archives the previous name into aliases.
   - Validates `ResourceNotFoundException` handling for nonexistent IDs.
3. **`TrackServiceTest`** (2 tests):
   - Validates track creation and duration formatting (`"05:26"`).
   - Validates rejection of orphan tracks when `artistId` does not exist.
4. **`SecurityIntegrationTest`** (3 tests):
   - Validates public access to read endpoints (`/api/v1/featured/today`).
   - Validates HTTP 403 Forbidden for unauthenticated mutation attempts.
   - Validates HTTP 201 Created when authenticated with `ROLE_EDITOR`.

---

## 6. Cloud Scaling Blueprint (AWS Production Evolution)

Because the service maintains strict package boundaries, the modular monolith maps directly to enterprise AWS production infrastructure:

| Modular Monolith Component | AWS Production Target Architecture |
|---|---|
| `artist` & `track` domains | Decomposed into independent microservices running on **AWS ECS Fargate / EKS**. |
| `catalogue` read model | Synchronized via Change Data Capture (CDC / Debezium) to **Amazon OpenSearch** for sub-10ms full-text and faceted search across 50M+ tracks. |
| In-memory Caffeine cache | **Amazon DynamoDB** with TTL items (`AOTD#YYYY-MM-DD`) pre-warmed via scheduled AWS Lambda at 00:05 UTC. |
| Cross-domain communication | **Transactional Outbox → Amazon Kinesis / SNS → SQS** for asynchronous event streaming (`ArtistRenamedEvent`, `TrackAddedEvent`). |
| Media metadata storage | **Amazon S3** for audio master files, album artwork, and royalty statement exports. |
| Containerized deployment | **AWS ECR** images orchestrated via Terraform / Helm with Horizontal Pod Autoscaling (HPA). |

---

## 7. Pre-Populated Seed Data

Upon startup, Flyway automatically provisions 5 sample artists and tracks to test all capabilities immediately:
1. **Childish Gambino** (Aliases: *Donald Glover*, *mcDJ*) — Tracks: *Redbone*, *This Is America*, *Sober*.
2. **Daft Punk** (Aliases: *The Robots*) — Tracks: *Get Lucky*, *One More Time*.
3. **Eminem** (Aliases: *Marshall Mathers*, *Slim Shady*) — Track: *Lose Yourself*.
4. **David Bowie** (Aliases: *Ziggy Stardust*, *The Thin White Duke*) — Tracks: *Heroes*, *Space Oddity*.
5. **Lady Gaga** (Aliases: *Stefani Germanotta*) — Track: *Bad Romance*.
