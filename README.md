# Open Liberty Demo Application

A modern RESTful web application built with Open Liberty, Jakarta EE 10, and MicroProfile 6.1.

## Features

- RESTful API with JAX-RS
- User management system (CRUD operations)
- Web UI with vanilla JavaScript
- Health check endpoints
- MicroProfile configuration
- Unit and integration tests

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Quick Start

### Build the application
```bash
mvn clean package
```

### Run the application
```bash
mvn liberty:run
```

The application will be available at:
- Web UI: http://localhost:9080/
- API: http://localhost:9080/api
- Health: http://localhost:9080/api/health

### Run tests
```bash
# Unit tests only
mvn test

# Integration tests (requires running server)
mvn verify
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| GET | `/api/users/search?username={username}` | Search by username |
| GET | `/api/health` | Health check |
| GET | `/api/health/ready` | Readiness check |
| GET | `/api/health/live` | Liveness check |

## Project Structure

```
openliberty-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── model/          # Domain models
│   │   │   ├── rest/           # REST endpoints
│   │   │   ├── service/        # Business logic
│   │   │   └── config/         # Configuration classes
│   │   ├── liberty/config/     # Liberty server configuration
│   │   └── webapp/             # Web resources (HTML, CSS, JS)
│   └── test/                   # Unit and integration tests
└── pom.xml                     # Maven configuration
```

## Development

### Stop the server
```bash
mvn liberty:stop
```

### Create a deployable WAR
```bash
mvn clean package
```
The WAR file will be in `target/openliberty-demo.war`

### Hot reload during development
The Liberty Maven plugin supports hot reload. When running with `mvn liberty:run`, changes to Java files and web resources are automatically detected and deployed.

## Technologies

- **Open Liberty** - Jakarta EE and MicroProfile runtime
- **Jakarta EE 10** - Enterprise Java standards
- **MicroProfile 6.1** - Microservices specifications
- **JAX-RS** - RESTful web services
- **CDI** - Dependency injection
- **JUnit 5** - Unit testing
- **REST Assured** - API integration testing