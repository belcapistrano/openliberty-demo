# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build and Run
```bash
# Build the application
mvn clean package

# Run the application (starts Liberty server)
mvn liberty:run

# Stop the server
mvn liberty:stop
```

### Testing
```bash
# Run unit tests only
mvn test

# Run single test class
mvn test -Dtest=UserServiceTest

# Run integration tests (requires running server)
mvn verify

# Run with specific test
mvn verify -Dit.test=UserResourceIT
```

### Development
```bash
# Run in dev mode with hot reload
mvn liberty:dev

# Package as WAR
mvn clean package
```

## Architecture

### Core Components

1. **REST API Layer** (`src/main/java/com/example/demo/rest/`)
   - JAX-RS resources expose RESTful endpoints
   - All endpoints are prefixed with `/api` via `@ApplicationPath` in `DemoApplication.java`
   - CORS is handled by `CORSFilter` for cross-origin requests

2. **Service Layer** (`src/main/java/com/example/demo/service/`)
   - Business logic encapsulated in CDI beans (@ApplicationScoped)
   - In-memory data storage for demo purposes (can be replaced with JPA/database)

3. **Model Layer** (`src/main/java/com/example/demo/model/`)
   - POJOs representing domain entities
   - Automatically serialized/deserialized to JSON by JAX-RS

4. **Web UI** (`src/main/webapp/`)
   - Static HTML/CSS/JavaScript frontend
   - Communicates with backend via REST API
   - No frontend framework dependencies (vanilla JS)

### Configuration

- **Liberty Server**: `src/main/liberty/config/server.xml`
  - Features: Jakarta EE 10, MicroProfile 6.1
  - HTTP port: 9080, HTTPS port: 9443
  - Auto-expand WAR files enabled

- **Maven**: `pom.xml`
  - Java 17 target
  - Liberty Maven plugin for server management
  - Testing with JUnit 5 and REST Assured

### API Patterns

All REST endpoints follow standard conventions:
- GET for retrieval
- POST for creation (returns 201 Created)
- PUT for updates
- DELETE for removal (returns 204 No Content)
- Proper HTTP status codes for errors (400, 404, 500)

### Testing Strategy

1. **Unit Tests**: Test service layer logic in isolation
2. **Integration Tests**: Test REST endpoints with running server (use RestAssured)
3. Test files end with `Test.java` (unit) or `IT.java` (integration)