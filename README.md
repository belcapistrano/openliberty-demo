# Open Liberty Demo Application

A modern RESTful web application built with Open Liberty, Jakarta EE 10, and MicroProfile 6.1.

## Features

- RESTful API with JAX-RS
- User management system (CRUD operations)
- Interactive web UI with vanilla JavaScript
- **Test Runner Web Interface** - Execute and monitor tests through browser
- Health check endpoints
- MicroProfile configuration
- Unit and integration tests
- Real-time test execution monitoring

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
- **User Management UI**: http://localhost:9080/openliberty-demo/
- **Test Runner UI**: http://localhost:9080/openliberty-demo/tests.html
- **API**: http://localhost:9080/openliberty-demo/api
- **Health Check**: http://localhost:9080/openliberty-demo/api/health

### Run tests

#### Command Line
```bash
# Unit tests only
mvn test

# Integration tests (requires running server)
mvn verify
```

#### Web Interface
1. Start the application: `mvn liberty:run`
2. Open the **Test Runner**: http://localhost:9080/openliberty-demo/tests.html
3. Click **"Run All Tests"** or select specific tests to run
4. Monitor test execution in real-time with detailed results and output

## API Endpoints

### User Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| GET | `/api/users/search?username={username}` | Search by username |

### Test Runner
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tests/available` | Get list of available tests |
| POST | `/api/tests/run` | Run all tests |
| POST | `/api/tests/run/{class}/{method}` | Run specific test |
| GET | `/api/tests/execution/{id}` | Get test execution details |
| GET | `/api/tests/executions` | Get all test executions |
| GET | `/api/tests/status/{id}` | Get test execution status |

### Health & Monitoring
| Method | Endpoint | Description |
|--------|----------|-------------|
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

## Test Runner Features

The integrated Test Runner provides a comprehensive web-based testing interface:

### Key Features
- **Real-time Test Execution**: Monitor test progress with live updates
- **Interactive Test Selection**: Run all tests or select specific test classes/methods
- **Detailed Results**: View test outcomes with timing and status information
- **Test Output Console**: See live test output and execution logs
- **Execution History**: Browse previous test runs with clickable results
- **Status Indicators**: Visual indicators for test states (passed, failed, running)
- **Test Statistics**: Summary of total, passed, failed, and skipped tests

### Available Tests
- **UserServiceTest**: Unit tests for user service logic (6 tests)
- **UserResourceIT**: Integration tests for REST API endpoints (7 tests)

### Test Runner Interface
1. **Control Panel**: Start test execution and select specific tests
2. **Results Grid**: Real-time display of individual test results
3. **Output Console**: Live streaming of test execution output
4. **Summary Dashboard**: Test statistics and execution metrics
5. **History Panel**: Previous test executions with quick access

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